package com.example.testsupport.framework.api.client;

import com.example.testsupport.framework.api.client.annotations.RequestHeaderParam;
import com.example.testsupport.framework.api.client.annotations.RequestQueryParam;
import com.example.testsupport.framework.api.client.params.GamblingBrandsParams;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class GamblingBrandsParamsTest {

    @Test
    void fieldsAreAnnotated() throws Exception {
        assertHeader("platformNodeId", "Platform-NodeId");
        assertHeader("platformLocale", "Platform-Locale");
        assertQuery("deviceType", "deviceType");
        assertQuery("showRestricted", "showRestricted");
        assertQuery("categoryAlias", "categoryAlias");
    }

    private void assertHeader(String fieldName, String expected) throws Exception {
        Field field = GamblingBrandsParams.class.getDeclaredField(fieldName);
        RequestHeaderParam ann = field.getAnnotation(RequestHeaderParam.class);
        assertNotNull(ann);
        assertEquals(expected, ann.value());
    }

    private void assertQuery(String fieldName, String expected) throws Exception {
        Field field = GamblingBrandsParams.class.getDeclaredField(fieldName);
        RequestQueryParam ann = field.getAnnotation(RequestQueryParam.class);
        assertNotNull(ann);
        assertEquals(expected, ann.value());
    }
}
