package com.example.testsupport.framework.api.client;

import com.example.testsupport.framework.api.client.annotations.RequestQueryParam;
import com.example.testsupport.framework.api.client.params.GamblingGamesParams;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class GamblingGamesParamsTest {

    @Test
    void fieldsAreAnnotated() throws Exception {
        Field field = GamblingGamesParams.class.getDeclaredField("brandAliasArray");
        RequestQueryParam ann = field.getAnnotation(RequestQueryParam.class);
        assertNotNull(ann);
        assertEquals("brandAliasArray", ann.value());
    }
}

