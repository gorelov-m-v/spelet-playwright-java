package com.example.testsupport.framework.device;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * JUnit adapter that obtains test matrix data from {@link TestMatrixService}.
 */
public class DeviceProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        ApplicationContext ctx = SpringExtension.getApplicationContext(context);
        TestMatrixService service = ctx.getBean(TestMatrixService.class);
        return service.getTestMatrix().map(values -> Arguments.of(values.toArray()));
    }
}
