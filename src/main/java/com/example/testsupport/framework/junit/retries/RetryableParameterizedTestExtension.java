package com.example.testsupport.framework.junit.retries;

import com.example.testsupport.framework.junit.retries.RetryableParameterizedTest;
import io.github.artsok.internal.RepeatedIfException;
import io.github.artsok.internal.ParameterizedRepeatedIfExceptionsTestNameFormatter;
import io.github.artsok.internal.ParameterizedRepeatedMethodContext;
import io.github.artsok.internal.ParameterizedTestInvocationContext;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.AnnotationConsumerInitializer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.opentest4j.TestAbortedException;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.toIntExact;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.junit.platform.commons.util.AnnotationUtils.*;

/**
 * Extension for {@link RetryableParameterizedTest}
 */
public class RetryableParameterizedTestExtension implements TestTemplateInvocationContextProvider,
        BeforeTestExecutionCallback, AfterTestExecutionCallback, TestExecutionExceptionHandler {

    private int totalRepeats = 0;
    private int minSuccess = 1;
    private List<Class<? extends Throwable>> repeatableExceptions;
    private boolean repeatableExceptionAppeared = false;
    private final List<Boolean> historyExceptionAppear = Collections.synchronizedList(new ArrayList<>());
    private static final String METHOD_CONTEXT_KEY = "context";
    private long suspend = 0L;

    @Override
    public boolean supportsTestTemplate(ExtensionContext extensionContext) {
        if (!extensionContext.getTestMethod().isPresent()) {
            return false;
        }

        Method testMethod = extensionContext.getTestMethod().get();
        if (!isAnnotated(testMethod, RetryableParameterizedTest.class)) {
            return false;
        }

        ParameterizedRepeatedMethodContext methodContext = new ParameterizedRepeatedMethodContext(testMethod);

        Preconditions.condition(methodContext.hasPotentiallyValidSignature(),
                () -> String.format(
                        "@RetryableParameterizedTest method [%s] declares formal parameters in an invalid order: "
                                + "argument aggregators must be declared after any indexed arguments "
                                + "and before any arguments resolved by another ParameterResolver.",
                        testMethod.toGenericString()));

        getStore(extensionContext).put(METHOD_CONTEXT_KEY, methodContext);
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        Method templateMethod = extensionContext.getRequiredTestMethod();
        String displayName = extensionContext.getDisplayName();
        ParameterizedRepeatedMethodContext methodContext = getStore(extensionContext)//
                .get(METHOD_CONTEXT_KEY, ParameterizedRepeatedMethodContext.class);
        ParameterizedRepeatedIfExceptionsTestNameFormatter formatter = createNameFormatter(templateMethod, displayName);

        RetryableParameterizedTest annotationParams = extensionContext.getTestMethod()
                .flatMap(testMethods -> findAnnotation(testMethods, RetryableParameterizedTest.class))
                .orElseThrow(() -> new RepeatedIfException("The extension should not be executed "
                        + "unless the test method is annotated with @RetryableParameterizedTest."));

        totalRepeats = annotationParams.repeats();
        minSuccess = annotationParams.minSuccess();
        suspend = annotationParams.suspend();

        String strTotalRepeats = System.getProperty("totalRepeats");
        if (strTotalRepeats != null) {
            try {
                totalRepeats = Integer.parseInt(strTotalRepeats);
            } catch (Exception ignored) {
            }
        }

        String strMinSuccess = System.getProperty("minSuccess");
        if (strMinSuccess != null) {
            try {
                minSuccess = Integer.parseInt(strMinSuccess);
            } catch (Exception ignored) {
            }
        }

        Preconditions.condition(totalRepeats > 0, "Total repeats must be higher than 0");
        Preconditions.condition(minSuccess >= 1, "Total minimum success must be higher or equals than 1");


        List<Object[]> collect = findRepeatableAnnotations(templateMethod, ArgumentsSource.class)
                .stream()
                .map(ArgumentsSource::value)
                .map(this::instantiateArgumentsProvider)
                .map(provider -> AnnotationConsumerInitializer.initialize(templateMethod, provider))
                .flatMap(provider -> arguments(provider, extensionContext))
                .map(Arguments::get)
                .map(arguments -> consumedArguments(arguments, methodContext))
                .collect(Collectors.toList());

        Spliterator<TestTemplateInvocationContext> spliterator =
                spliteratorUnknownSize(new TestTemplateIteratorParams(collect, formatter, methodContext), Spliterator.NONNULL);
        return stream(spliterator, false);

    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        repeatableExceptions = Stream.of(context.getTestMethod()
                .flatMap(testMethods -> findAnnotation(testMethods, RetryableParameterizedTest.class))
                .orElseThrow(() -> new IllegalStateException("The extension should not be executed "))
                .exceptions()
        ).collect(Collectors.toList());
        repeatableExceptions.add(TestAbortedException.class);
    }

    //Записываем в historyExceptionAppear по конкретным аргументам!
    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            historyExceptionAppear.add(false);
            repeatableExceptionAppeared = false;
        }
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        if (appearedExceptionDoesNotAllowRepetitions(throwable)) {
            throw throwable;
        }

        historyExceptionAppear.add(true);
        repeatableExceptionAppeared = true;

        Allure.getLifecycle().updateTestCase(tr -> {
            try {
                tr.getClass().getMethod("setRetry", Boolean.class).invoke(tr, true);
            } catch (Exception ignored) {
            }
            tr.setStatus(Status.FAILED);
            tr.setStatusDetails(new StatusDetails().setMessage(throwable.getMessage()));
        });

        long successCount = historyExceptionAppear.stream().filter(b -> !b).count();
        boolean canStillReach = isMinSuccessTargetStillReachable(minSuccess);

        if (successCount < minSuccess && canStillReach) {
            if (suspend > 0) {
                try {
                    Thread.sleep(suspend);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            throw new TestAbortedException("Retry due to repeatable exception", throwable);
        }

        throw throwable;
    }

    /**
     * If cannot reach a minimum success target, will return true
     *
     * @param minSuccessCount - minimum success count
     * @return true/false
     */
    private boolean isMinSuccessTargetStillReachable(final long minSuccessCount) {
        return historyExceptionAppear.stream().filter(bool -> bool).count() <= totalRepeats - minSuccessCount;
    }

    private boolean appearedExceptionDoesNotAllowRepetitions(Throwable appearedException) {
        return repeatableExceptions.stream().noneMatch(ex -> ex.isAssignableFrom(appearedException.getClass()));
    }

    private ParameterizedRepeatedIfExceptionsTestNameFormatter createNameFormatter(Method templateMethod, String displayName) {
        RetryableParameterizedTest parameterizedTest = findAnnotation(templateMethod, RetryableParameterizedTest.class).get();
        String pattern = Preconditions.notBlank(parameterizedTest.name().trim(),
                () -> String.format(
                        "Configuration error: @RetryableParameterizedTest on method [%s] must be declared with a non-empty name.",
                        templateMethod));

        String repeatedPattern = Preconditions.notBlank(parameterizedTest.repeatedName(), () -> String.format(
                "Configuration error: @RetryableParameterizedTest on method [%s] must be declared with a non-empty repeated name.",
                templateMethod));

        return new ParameterizedRepeatedIfExceptionsTestNameFormatter(pattern, displayName, repeatedPattern);
    }

    protected static Stream<? extends Arguments> arguments(ArgumentsProvider provider, ExtensionContext context) {
        try {
            return provider.provideArguments(context);
        } catch (Exception e) {
            throw ExceptionUtils.throwAsUncheckedException(e);
        }
    }

    private Object[] consumedArguments(Object[] arguments, ParameterizedRepeatedMethodContext methodContext) {
        int parameterCount = methodContext.getParameterCount();
        return methodContext.hasAggregator() ? arguments
                : (arguments.length > parameterCount ? Arrays.copyOf(arguments, parameterCount) : arguments);
    }

    private ArgumentsProvider instantiateArgumentsProvider(Class<? extends ArgumentsProvider> clazz) {
        try {
            return ReflectionUtils.newInstance(clazz);
        } catch (Exception ex) {
            if (ex instanceof NoSuchMethodException) {
                String message = String.format("Failed to find a no-argument constructor for ArgumentsProvider [%s]. "
                                + "Please ensure that a no-argument constructor exists and "
                                + "that the class is either a top-level class or a static nested class",
                        clazz.getName());
                throw new JUnitException(message, ex);
            }
            throw ex;
        }
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(RetryableParameterizedTestExtension.class, context.getRequiredTestMethod()));
    }

    /**
     * TestTemplateIteratorParams (Repeat test if it failed)
     */
    class TestTemplateIteratorParams implements Iterator<TestTemplateInvocationContext> {

        private final List<Object[]> params;
        private final ParameterizedRepeatedIfExceptionsTestNameFormatter formatter;
        private final ParameterizedRepeatedMethodContext methodContext;
        private final AtomicLong invocationCount;
        private final AtomicLong paramsCount;
        private int currentIndex = 0;

        TestTemplateIteratorParams(List<Object[]> arguments, final ParameterizedRepeatedIfExceptionsTestNameFormatter formatter, final ParameterizedRepeatedMethodContext methodContext) {
            this.params = arguments;
            this.formatter = formatter;
            this.methodContext = methodContext;
            this.invocationCount = new AtomicLong(params.size() - 1);
            this.paramsCount = new AtomicLong(0);
        }

        @Override
        public boolean hasNext() {
            boolean needRetry = historyExceptionAppear.contains(true) && currentIndex < totalRepeats;
            return needRetry || invocationCount.get() >= paramsCount.get();
        }

        /**
         * Return next ParameterizedTestInvocationContext. Managing several situations:
         * 1) Exception in Parameterized Test appears
         * 2) When the count of tests for one argument (parameter) equal total repeats
         * 3) If no exception appears start to create new  ParameterizedTestInvocationContext
         *
         * @return {@link ParameterizedTestInvocationContext}
         */
        @Override
        public TestTemplateInvocationContext next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            int currentParam = paramsCount.intValue();

            long lastSuccessWindow = historyExceptionAppear.stream()
                    .skip(Math.max(0, historyExceptionAppear.size() - minSuccess))
                    .filter(b -> !b)
                    .count();

            boolean lastWasFailure = !historyExceptionAppear.isEmpty()
                    && historyExceptionAppear.get(historyExceptionAppear.size() - 1);

            boolean needRetry = lastWasFailure
                    && currentIndex < totalRepeats
                    && lastSuccessWindow < minSuccess;

            if (needRetry) {
                if (suspend > 0) {
                    try {
                        Thread.sleep(suspend);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                currentIndex++;
                return new ParameterizedTestInvocationContext(currentIndex, totalRepeats, formatter, methodContext, params.get(currentParam));
            }

            paramsCount.incrementAndGet();
            currentIndex = 0;
            repeatableExceptionAppeared = false;
            historyExceptionAppear.clear();

            if (currentParam >= params.size()) {
                throw new NoSuchElementException();
            }
            return new ParameterizedTestInvocationContext(0, 0, formatter, methodContext, params.get(currentParam));
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
