package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class FlakyTestArgumentProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
                Arguments.of("Сценарий 'Успех с 1-й попытки'", 0),
                Arguments.of("Сценарий 'Успех после 1 падения'", 1),
                Arguments.of("Сценарий 'Успех после 2 падений'", 2),
                Arguments.of("Сценарий 'Всегда падает'", -1)
        );
    }
}
