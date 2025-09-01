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

    private static final String METHOD_CONTEXT_KEY = "context";
    private static final String TOTAL_REPEATS_KEY = "totalRepeats";
    private static final String MIN_SUCCESS_KEY = "minSuccess";
    private static final String SUSPEND_KEY = "suspend";
    private static final String REPEATABLE_EXCEPTIONS_KEY = "repeatableExceptions";
    private static final String REPEATABLE_EXCEPTION_APPEARED_KEY = "repeatableExceptionAppeared";
    private static final String HISTORY_KEY = "history";

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

        getConfigStore(extensionContext).put(METHOD_CONTEXT_KEY, methodContext);
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        Method templateMethod = extensionContext.getRequiredTestMethod();
        String displayName = extensionContext.getDisplayName();
        ParameterizedRepeatedMethodContext methodContext = getConfigStore(extensionContext)//
                .get(METHOD_CONTEXT_KEY, ParameterizedRepeatedMethodContext.class);
        ParameterizedRepeatedIfExceptionsTestNameFormatter formatter = createNameFormatter(templateMethod, displayName);

        RetryableParameterizedTest annotationParams = extensionContext.getTestMethod()
                .flatMap(testMethods -> findAnnotation(testMethods, RetryableParameterizedTest.class))
                .orElseThrow(() -> new RepeatedIfException("The extension should not be executed "
                        + "unless the test method is annotated with @RetryableParameterizedTest."));

        int totalRepeats = annotationParams.repeats();
        int minSuccess = annotationParams.minSuccess();
        long suspend = annotationParams.suspend();

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

        ExtensionContext.Store store = getConfigStore(extensionContext);
        store.put(TOTAL_REPEATS_KEY, totalRepeats);
        store.put(MIN_SUCCESS_KEY, minSuccess);
        store.put(SUSPEND_KEY, suspend);


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
                spliteratorUnknownSize(new TestTemplateIteratorParams(collect, formatter, methodContext, extensionContext), Spliterator.NONNULL);
        return stream(spliterator, false);

    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        List<Class<? extends Throwable>> repeatableExceptions = Stream.of(context.getTestMethod()
                .flatMap(testMethods -> findAnnotation(testMethods, RetryableParameterizedTest.class))
                .orElseThrow(() -> new IllegalStateException("The extension should not be executed "))
                .exceptions()
        ).collect(Collectors.toList());
        repeatableExceptions.add(TestAbortedException.class);
        getConfigStore(context).put(REPEATABLE_EXCEPTIONS_KEY, repeatableExceptions);
    }

    //Записываем в historyExceptionAppear по конкретным аргументам!
    @Override
    public void afterTestExecution(ExtensionContext context) {
        boolean exceptionAppeared = exceptionAppeared(context);
        List<Boolean> history = getHistory(context);
        history.add(exceptionAppeared);
    }

    private boolean exceptionAppeared(ExtensionContext extensionContext) {
        if (extensionContext.getExecutionException().isPresent()) {
            Throwable exception = extensionContext.getExecutionException().get();
            List<Class<? extends Throwable>> repeatableExceptions = getConfigStore(extensionContext).get(REPEATABLE_EXCEPTIONS_KEY, List.class);
            return isExceptionRetryable(exception, repeatableExceptions);
        }
        return false;
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        List<Class<? extends Throwable>> repeatableExceptions = getConfigStore(context).get(REPEATABLE_EXCEPTIONS_KEY, List.class);
        if (!isExceptionRetryable(throwable, repeatableExceptions)) {
            System.out.printf("Test failed with exception chain [%s] which is not configured for retry. Failing fast.%n",
                    buildExceptionChain(throwable));
            throw throwable;
        }
        setRepeatableExceptionAppeared(context, true);

        List<Boolean> history = getHistory(context);
        int minSuccess = getConfigStore(context).get(MIN_SUCCESS_KEY, Integer.class);
        long currentSuccessCount = history.stream().filter(exceptionAppeared -> !exceptionAppeared).count();
        if (currentSuccessCount < minSuccess && isMinSuccessTargetStillReachable(context, minSuccess)) {
            throw new TestAbortedException("Do not fail completely, but repeat the test", throwable);
        }
        throw throwable;
    }

    /**
     * If cannot reach a minimum success target, will return true
     *
     * @param minSuccessCount - minimum success count
     * @return true/false
     */
    private boolean isMinSuccessTargetStillReachable(ExtensionContext context, final long minSuccessCount) {
        List<Boolean> history = getHistory(context);
        int totalRepeats = getConfigStore(context).get(TOTAL_REPEATS_KEY, Integer.class);
        return history.stream().filter(bool -> bool).count() <= totalRepeats - minSuccessCount;
    }

    private boolean isExceptionRetryable(Throwable throwable, List<Class<? extends Throwable>> retryableExceptions) {
        if (throwable == null) {
            return false;
        }
        for (Class<? extends Throwable> ex : retryableExceptions) {
            if (ex.isAssignableFrom(throwable.getClass())) {
                return true;
            }
        }
        return isExceptionRetryable(throwable.getCause(), retryableExceptions);
    }

    private String buildExceptionChain(Throwable throwable) {
        List<String> chain = new ArrayList<>();
        Throwable current = throwable;
        while (current != null) {
            chain.add(current.getClass().getName());
            current = current.getCause();
        }
        return String.join(" -> ", chain);
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
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()));
    }

    private ExtensionContext.Store getConfigStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }

    @SuppressWarnings("unchecked")
    private List<Boolean> getHistory(ExtensionContext context) {
        ExtensionContext.Store store = getStore(context);
        return (List<Boolean>) store.getOrComputeIfAbsent(HISTORY_KEY, key -> Collections.synchronizedList(new ArrayList<>()), List.class);
    }

    private void setRepeatableExceptionAppeared(ExtensionContext context, boolean appeared) {
        getStore(context).put(REPEATABLE_EXCEPTION_APPEARED_KEY, appeared);
    }

    private boolean getRepeatableExceptionAppeared(ExtensionContext context) {
        Boolean value = getStore(context).get(REPEATABLE_EXCEPTION_APPEARED_KEY, Boolean.class);
        return value != null && value;
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
        private final ExtensionContext extensionContext;

        TestTemplateIteratorParams(List<Object[]> arguments, final ParameterizedRepeatedIfExceptionsTestNameFormatter formatter, final ParameterizedRepeatedMethodContext methodContext, ExtensionContext extensionContext) {
            this.params = arguments;
            this.formatter = formatter;
            this.methodContext = methodContext;
            this.invocationCount = new AtomicLong(params.size() - 1);
            this.paramsCount = new AtomicLong(0);
            this.extensionContext = extensionContext;
        }

        @Override
        public boolean hasNext() {
        int totalRepeats = getConfigStore(extensionContext).get(TOTAL_REPEATS_KEY, Integer.class);
            List<Boolean> history = getHistory(extensionContext);
            if (!history.isEmpty()
                    && history.get(history.size() - 1)
                    && currentIndex < totalRepeats) {
                return true;
            }
            return invocationCount.get() >= paramsCount.get();
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

            if (hasNext()) {
                int currentParam = paramsCount.intValue();
                List<Boolean> history = getHistory(extensionContext);
                int totalRepeats = getConfigStore(extensionContext).get(TOTAL_REPEATS_KEY, Integer.class);
                int minSuccess = getConfigStore(extensionContext).get(MIN_SUCCESS_KEY, Integer.class);
                int errorTestRepetitionsCountForOneArgument = toIntExact(history.stream().filter(b -> b).count());
                int successfulTestRepetitionsCountForOneArgument = toIntExact(history
                        .stream()
                        .skip(history.size() - minSuccess <= 0 ? 0 : history.size() - minSuccess)
                        .filter(b -> !b)
                        .count());

                if (errorTestRepetitionsCountForOneArgument >= 1 && currentIndex < totalRepeats && successfulTestRepetitionsCountForOneArgument != minSuccess) {

                    //If exception appeared would wait suspend time
                    long suspend = getConfigStore(extensionContext).get(SUSPEND_KEY, Long.class);
                    if (history.stream().anyMatch(ex -> ex) && suspend != 0L) {
                        try {
                            Thread.sleep(suspend);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    currentIndex++;
                    setRepeatableExceptionAppeared(extensionContext, false);
                    return new ParameterizedTestInvocationContext(currentIndex, totalRepeats, formatter, methodContext, params.get(currentParam - 1));
                }

                if (currentIndex == totalRepeats || !getRepeatableExceptionAppeared(extensionContext)) {
                    paramsCount.incrementAndGet();
                    setRepeatableExceptionAppeared(extensionContext, false);
                    history.clear();
                }

                currentIndex = 0;
                return new ParameterizedTestInvocationContext(0, 0, formatter, methodContext, params.get(currentParam));
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
