package com.example.testsupport.framework.api.client;

import com.example.testsupport.framework.api.client.annotations.RequestQueryParam;
import com.example.testsupport.framework.api.client.params.GamblingGamesParams;
import com.example.testsupport.framework.allure.Suite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@Suite("Параметры игр")
@DisplayName("Аннотации параметров игр")
class GamblingGamesParamsTest {

    @Test
    @Tag("Unit-test")
    @DisplayName("Поле brandAliasArray помечено как параметр запроса")
    void fieldsAreAnnotated() throws Exception {
        Field field = GamblingGamesParams.class.getDeclaredField("brandAliasArray");
        RequestQueryParam ann = field.getAnnotation(RequestQueryParam.class);
        assertNotNull(ann);
        assertEquals("brandAliasArray", ann.value());
    }
}

