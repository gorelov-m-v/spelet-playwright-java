package com.example.testsupport.framework.api.client;

import com.example.testsupport.framework.api.client.annotations.RequestHeaderParam;
import com.example.testsupport.framework.api.client.annotations.RequestQueryParam;
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
        // 1. Extract parameter object from the template body
        Object params = template.body();
        if (Objects.isNull(params)) {
            return;
        }

        // 2. Map annotated fields to query params and headers
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
                // ignore inaccessible field
            }
        }

        // 3. Clear the body to avoid sending GET request with body
        template.body((String) null);
    }
}

