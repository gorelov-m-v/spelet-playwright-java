package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class FlakyTestArgumentProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
                Arguments.of("success"),
                Arguments.of("failOnce"),
                Arguments.of("failTwice"),
                Arguments.of("alwaysFail")
        );
    }
}
