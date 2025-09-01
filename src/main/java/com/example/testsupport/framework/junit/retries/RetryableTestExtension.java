/*
 * (C) Copyright 2017 Artem Sokovets (http://github.com/artsok/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.example.testsupport.framework.junit.retries;


import com.example.testsupport.framework.junit.retries.RetryableTest;
import io.github.artsok.internal.RepeatedIfException;
import io.github.artsok.internal.RepeatedIfExceptionsDisplayNameFormatter;
import io.github.artsok.internal.RepeatedIfExceptionsInvocationContext;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.TestAbortedException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.toIntExact;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;


/**
 * Main condition for extension point @RepeatedIfExceptions
 * All logic in this class. See TestTemplateIterator where handler logic of repeat tests
 *
 * @author Artem Sokovets
 */
public class RetryableTestExtension implements TestTemplateInvocationContextProvider, BeforeTestExecutionCallback,
        AfterTestExecutionCallback, TestExecutionExceptionHandler {


    private static final int CURRENT_RUN = 1;
    private static final String REPEATS_KEY = "repeats";
    private static final String MIN_SUCCESS_KEY = "minSuccess";
    private static final String TOTAL_RUNS_KEY = "totalRuns";
    private static final String REPEATABLE_EXCEPTIONS_KEY = "repeatableExceptions";
    private static final String REPEATABLE_EXCEPTION_APPEARED_KEY = "repeatableExceptionAppeared";
    private static final String HISTORY_KEY = "history";
    private static final String SUSPEND_KEY = "suspend";
    private RepeatedIfExceptionsDisplayNameFormatter formatter;


    /**
     * Check that test method contain {@link RetryableTest} annotation
     *
     * @param extensionContext - encapsulates the context in which the current test or container is being executed
     * @return true/false
     */
    @Override
    public boolean supportsTestTemplate(ExtensionContext extensionContext) {
        return isAnnotated(extensionContext.getTestMethod(), RetryableTest.class);
    }


    /**
     * Context call TestTemplateInvocationContext
     *
     * @param extensionContext - Test Class Context
     * @return Stream of TestTemplateInvocationContext
     */
    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        Preconditions.notNull(extensionContext.getTestMethod().orElse(null), "Test method must not be null");

        RetryableTest annotationParams = extensionContext.getTestMethod()
                .flatMap(testMethods -> findAnnotation(testMethods, RetryableTest.class))
                .orElseThrow(() -> new RepeatedIfException("The extension should not be executed "
                        + "unless the test method is annotated with @RetryableTest."));


        int totalRepeats = annotationParams.repeats();
        int minSuccess = annotationParams.minSuccess();
        Preconditions.condition(totalRepeats > 0, "Total repeats must be higher than 0");
        Preconditions.condition(minSuccess >= 1, "Total minimum success must be higher or equals than 1");

        int totalTestRuns = totalRepeats + CURRENT_RUN;
        long suspend = annotationParams.suspend();

        ExtensionContext.Store configStore = getConfigStore(extensionContext);
        configStore.put(REPEATS_KEY, totalRepeats);
        configStore.put(MIN_SUCCESS_KEY, minSuccess);
        configStore.put(TOTAL_RUNS_KEY, totalTestRuns);
        configStore.put(SUSPEND_KEY, suspend);

        String displayName = extensionContext.getDisplayName();
        formatter = displayNameFormatter(annotationParams, displayName);

        //Convert logic of repeated handler to spliterator
        Spliterator<TestTemplateInvocationContext> spliterator =
                spliteratorUnknownSize(new TestTemplateIterator(extensionContext), Spliterator.NONNULL);
        return stream(spliterator, false);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
            
        //get TotalTestRuns and minSuccess from system properties
        ExtensionContext.Store store = getConfigStore(context);
        Integer totalRuns = store.get(TOTAL_RUNS_KEY, Integer.class);
        Integer minSuccess = store.get(MIN_SUCCESS_KEY, Integer.class);
        String strTotalRepeats = System.getProperty("totalRepeats");
        if(strTotalRepeats != null) {
            try {
                totalRuns = Integer.parseInt(strTotalRepeats);
            }catch(Exception e){
            }
        }

        String strMinSuccess = System.getProperty("minSuccess");
        if(strMinSuccess != null) {
            try {
                minSuccess = Integer.parseInt(strMinSuccess);
            }catch(Exception e){
            }
        }

        store.put(TOTAL_RUNS_KEY, totalRuns);
        store.put(MIN_SUCCESS_KEY, minSuccess);

        List<Class<? extends Throwable>> repeatableExceptions = Stream.of(context.getTestMethod()
                .flatMap(testMethods -> findAnnotation(testMethods, RetryableTest.class))
                .orElseThrow(() -> new IllegalStateException("The extension should not be executed "))
                .exceptions()
        ).collect(Collectors.toList());
        repeatableExceptions.add(TestAbortedException.class);
        store.put(REPEATABLE_EXCEPTIONS_KEY, repeatableExceptions);
    }

    /**
     * Check if exceptions that will appear in test same as we wait
     *
     * @param extensionContext - Test Class Context
     */
    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        boolean exceptionAppeared = exceptionAppeared(extensionContext);
        getHistory(extensionContext).add(exceptionAppeared);
    }

    private boolean exceptionAppeared(ExtensionContext extensionContext) {
        if (extensionContext.getExecutionException().isEmpty()) {
            return false;
        }
        Throwable exception = extensionContext.getExecutionException().get();
        List<Class<? extends Throwable>> repeatableExceptions = getConfigStore(extensionContext).get(REPEATABLE_EXCEPTIONS_KEY, List.class);
        return isExceptionRetryable(exception, repeatableExceptions);
    }

    /**
     * Handler for display name
     *
     * @param test        - RetryableTest annotation
     * @param displayName - Name that will be represent to report
     * @return RepeatedIfExceptionsDisplayNameFormatter {@link RepeatedIfExceptionsDisplayNameFormatter}
     */
    private RepeatedIfExceptionsDisplayNameFormatter displayNameFormatter(RetryableTest test, String displayName) {
        String pattern = test.name().trim();
        if (StringUtils.isBlank(pattern)) {
            pattern = Optional.of(test.name())
                    .orElseThrow(() -> new RepeatedIfException("Exception occurred with name parameter of RetryableTest annotation"));
        }
        return new RepeatedIfExceptionsDisplayNameFormatter(pattern, displayName);
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

    private ExtensionContext.Store getConfigStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }

    private ExtensionContext.Store getRunStore(ExtensionContext context) {
        ExtensionContext methodContext = context;
        while (methodContext.getParent().isPresent() && methodContext.getParent().get().getTestMethod().isPresent()) {
            methodContext = methodContext.getParent().get();
        }
        return methodContext.getStore(ExtensionContext.Namespace.create(getClass(), methodContext.getUniqueId()));
    }

    @SuppressWarnings("unchecked")
    private List<Boolean> getHistory(ExtensionContext context) {
        ExtensionContext.Store store = getRunStore(context);
        return (List<Boolean>) store.getOrComputeIfAbsent(HISTORY_KEY,
                key -> Collections.synchronizedList(new ArrayList<>()), List.class);
    }

    private void setRepeatableExceptionAppeared(ExtensionContext context, boolean appeared) {
        getRunStore(context).put(REPEATABLE_EXCEPTION_APPEARED_KEY, appeared);
    }

    private boolean getRepeatableExceptionAppeared(ExtensionContext context) {
        Boolean value = getRunStore(context).get(REPEATABLE_EXCEPTION_APPEARED_KEY, Boolean.class);
        return value != null && value;
    }

    /**
     * If cannot reach a minimum success target, will return true
     *
     * @param minSuccessCount - minimum success count
     * @return true/false
     */
    private boolean isMinSuccessTargetStillReachable(ExtensionContext context, final long minSuccessCount) {
        List<Boolean> history = getHistory(context);
        int totalRuns = getConfigStore(context).get(TOTAL_RUNS_KEY, Integer.class);
        return history.stream().filter(bool -> bool).count() < totalRuns - minSuccessCount;
    }

    /**
     * TestTemplateIterator (Repeat test if it failed)
     */
    class TestTemplateIterator implements Iterator<TestTemplateInvocationContext> {
        int currentIndex = 0;
        private final ExtensionContext extensionContext;

        TestTemplateIterator(ExtensionContext extensionContext) {
            this.extensionContext = extensionContext;
        }

        @Override
        public boolean hasNext() {
            if (currentIndex == 0) {
                return true;
            }
            List<Boolean> history = getHistory(extensionContext);
            int totalRuns = getConfigStore(extensionContext).get(TOTAL_RUNS_KEY, Integer.class);
            return history.stream().anyMatch(ex -> ex) && currentIndex < totalRuns;
        }

        @Override
        public TestTemplateInvocationContext next() {
            //If exception appeared would wait suspend time
            List<Boolean> history = getHistory(extensionContext);
            long suspend = getConfigStore(extensionContext).get(SUSPEND_KEY, Long.class);
            if (history.stream().anyMatch(ex -> ex) && suspend != 0L) {
                try {
                    Thread.sleep(suspend);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            int successfulTestRepetitionsCount = toIntExact(history.stream().filter(b -> !b).count());
            if (hasNext()) {
                currentIndex++;
                int totalRuns = getConfigStore(extensionContext).get(TOTAL_RUNS_KEY, Integer.class);
                int minSuccess = getConfigStore(extensionContext).get(MIN_SUCCESS_KEY, Integer.class);
                boolean appeared = getRepeatableExceptionAppeared(extensionContext);
                return new RepeatedIfExceptionsInvocationContext(currentIndex, totalRuns,
                        successfulTestRepetitionsCount, minSuccess, appeared, formatter);
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
