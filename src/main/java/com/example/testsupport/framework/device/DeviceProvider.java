package com.example.testsupport.framework.device;

import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.api.Named.named;

/**
 * JUnit adapter that obtains test matrix data from {@link TestMatrixService}.
 */
public class DeviceProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        ApplicationContext ctx = SpringExtension.getApplicationContext(context);
        TestMatrixService service = ctx.getBean(TestMatrixService.class);
        return service.getTestMatrix()
                .map(values -> new Object[]{(Device) values.get(0), (String) values.get(1)})
                .sorted(
                        Comparator.<Object[], String>comparing(a -> ((Device) a[0]).getName())
                                .thenComparing(a -> (String) a[1])
                )
                .map(a -> arguments(
                        named(((Device) a[0]).getName(), (Device) a[0]),
                        named((String) a[1], (String) a[1])
                ));
    }
}
