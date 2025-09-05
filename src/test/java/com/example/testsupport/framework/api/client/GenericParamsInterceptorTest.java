package com.example.testsupport.framework.api.client;

import com.example.testsupport.framework.api.client.params.GamblingBrandsParams;
import feign.RequestTemplate;
import feign.template.QueryTemplate;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GenericParamsInterceptorTest {

    @Test
    void mapsFieldsAndClearsParams() throws Exception {
        GamblingBrandsParams params = GamblingBrandsParams.builder()
                .platformNodeId("node-1")
                .platformLocale("en-US")
                .deviceType("mobile")
                .showRestricted(true)
                .categoryAlias("slots")
                .build();

        RequestTemplate template = new RequestTemplate();
        QueryTemplate qt = QueryTemplate.create("params", Collections.emptyList(), StandardCharsets.UTF_8);
        java.lang.reflect.Field valuesField = QueryTemplate.class.getDeclaredField("values");
        valuesField.setAccessible(true);
        valuesField.set(qt, Collections.singletonList(params));
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("params", qt);
        java.lang.reflect.Field field = RequestTemplate.class.getDeclaredField("queries");
        field.setAccessible(true);
        field.set(template, raw);

        new GenericParamsInterceptor().apply(template);

        assertEquals("mobile", template.queries().get("deviceType").iterator().next());
        assertEquals("true", template.queries().get("showRestricted").iterator().next());
        assertEquals("slots", template.queries().get("categoryAlias").iterator().next());
        assertEquals("node-1", template.headers().get("Platform-NodeId").iterator().next());
        assertEquals("en-US", template.headers().get("Platform-Locale").iterator().next());
        assertFalse(template.queries().containsKey("params"));
    }

    @Test
    void ignoresWhenNoParamsPresent() {
        RequestTemplate template = new RequestTemplate();
        new GenericParamsInterceptor().apply(template);
        assertTrue(template.queries().isEmpty());
    }
}
