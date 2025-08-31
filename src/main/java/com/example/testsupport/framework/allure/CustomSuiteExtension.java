package com.example.testsupport.framework.allure;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Label;
import java.util.Optional;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class CustomSuiteExtension implements BeforeTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        Suite suite = resolveSuite(context);
        if (suite == null) {
            return;
        }
        Allure.getLifecycle().updateTestCase(tc -> tc.getLabels().removeIf(l -> "suite".equals(l.getName())));
        Allure.getLifecycle().updateTestCase(tc -> tc.getLabels().add(new Label().setName("suite").setValue(suite.value())));
    }

    private Suite resolveSuite(ExtensionContext context) {
        Optional<Suite> methodSuite = context.getTestMethod().map(m -> m.getAnnotation(Suite.class));
        if (methodSuite.isPresent()) {
            return methodSuite.get();
        }
        return context.getTestClass().map(c -> c.getAnnotation(Suite.class)).orElse(null);
    }
}
