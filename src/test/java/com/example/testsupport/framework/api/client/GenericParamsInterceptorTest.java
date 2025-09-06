package com.example.testsupport.framework.api.client;

import com.example.testsupport.framework.api.client.params.GamblingBrandsParams;
import com.example.testsupport.framework.allure.Suite;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@Suite("Перехватчик параметров")
@DisplayName("GenericParamsInterceptor")
class GenericParamsInterceptorTest {

    @Test
    @Tag("Unit-test")
    @DisplayName("Переносит аннотированные поля и очищает тело шаблона")
    void appliesAnnotationsAndClearsBody() throws Exception {
        GamblingBrandsParams params = GamblingBrandsParams.builder()
                .platformNodeId("node-1")
                .platformLocale("en-US")
                .deviceType("mobile")
                .showRestricted(true)
                .categoryAlias("slots")
                .build();

        RequestTemplate template = new RequestTemplate();
        template.method("GET");

        GenericParamsInterceptor interceptor = new GenericParamsInterceptor();
        interceptor.mapParams(params, template);

        Field bodyField = RequestTemplate.class.getDeclaredField("body");
        bodyField.setAccessible(true);
        bodyField.set(template, Request.Body.create("test", java.nio.charset.StandardCharsets.UTF_8));

        interceptor.apply(template);

        assertEquals("mobile", template.queries().get("deviceType").iterator().next());
        assertEquals("true", template.queries().get("showRestricted").iterator().next());
        assertEquals("slots", template.queries().get("categoryAlias").iterator().next());
        assertEquals("node-1", template.headers().get("Platform-NodeId").iterator().next());
        assertEquals("en-US", template.headers().get("Platform-Locale").iterator().next());
        assertNull(bodyField.get(template));
    }
}
