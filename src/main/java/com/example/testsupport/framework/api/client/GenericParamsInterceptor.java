package com.example.testsupport.framework.api.client;

import com.example.testsupport.framework.api.client.annotations.RequestHeaderParam;
import com.example.testsupport.framework.api.client.annotations.RequestQueryParam;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Generic interceptor that maps annotated fields of parameter objects to
 * request query parameters and headers.
 */
public class GenericParamsInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Object params = extractParams(template);
        if (Objects.nonNull(params)) {
            mapParams(params, template);
        }

        if ("GET".equalsIgnoreCase(template.method())) {
            clearBody(template);
        }
    }

    void mapParams(Object params, RequestTemplate template) {
        for (Field field : params.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(params);
                if (value == null) {
                    continue;
                }
                RequestQueryParam queryAnn = field.getAnnotation(RequestQueryParam.class);
                if (queryAnn != null) {
                    template.query(queryAnn.value(), value.toString());
                }
                RequestHeaderParam headerAnn = field.getAnnotation(RequestHeaderParam.class);
                if (headerAnn != null) {
                    template.header(headerAnn.value(), value.toString());
                }
            } catch (IllegalAccessException ignored) {
                // ignore inaccessible fields
            }
        }
    }

    private Object extractParams(RequestTemplate template) {
        try {
            Field field = RequestTemplate.class.getDeclaredField("body");
            field.setAccessible(true);
            Object value = field.get(template);
            return (value instanceof Request.Body) ? null : value;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private void clearBody(RequestTemplate template) {
        try {
            Field field = RequestTemplate.class.getDeclaredField("body");
            field.setAccessible(true);
            field.set(template, Request.Body.empty());
        } catch (ReflectiveOperationException ignored) {
            // ignore
        }
    }
}
