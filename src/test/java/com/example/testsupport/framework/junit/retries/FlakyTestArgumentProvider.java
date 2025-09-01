package com.example.testsupport.framework.junit.retries;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class FlakyTestArgumentProvider implements ArgumentsProvider {

    public enum Scenario { SUCCESS, FLAKY, FAIL }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
                Arguments.of(Scenario.SUCCESS),
                Arguments.of(Scenario.FLAKY),
                Arguments.of(Scenario.FAIL)
        );
    }
}

