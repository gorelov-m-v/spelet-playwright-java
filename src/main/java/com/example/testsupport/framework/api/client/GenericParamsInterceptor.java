package com.example.testsupport.framework.api.client;

import com.example.testsupport.framework.api.client.annotations.RequestHeaderParam;
import com.example.testsupport.framework.api.client.annotations.RequestQueryParam;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.template.QueryTemplate;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic interceptor that maps annotated fields of parameter objects to
 * request query parameters and headers.
 */
public class GenericParamsInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Object params = extractParams(template);
        clearParamsQuery(template);
        if (params == null) {
            return;
        }

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
    }

    private Object extractParams(RequestTemplate template) {
        Map<String, Object> rawQueries = getRawQueries(template);
        Object value = rawQueries.get("params");
        if (value == null) {
            return null;
        }
        if (value instanceof Collection<?> collection) {
            return collection.isEmpty() ? null : collection.iterator().next();
        }
        if (value instanceof QueryTemplate queryTemplate) {
            try {
                Field valuesField = QueryTemplate.class.getDeclaredField("values");
                valuesField.setAccessible(true);
                Object vals = valuesField.get(queryTemplate);
                if (vals instanceof Collection<?> coll) {
                    return coll.isEmpty() ? null : coll.iterator().next();
                }
            } catch (ReflectiveOperationException ignored) {
                // ignore reflection issues
            }
        }
        return null;
    }

    private Map<String, Object> getRawQueries(RequestTemplate template) {
        try {
            Field field = RequestTemplate.class.getDeclaredField("queries");
            field.setAccessible(true);
            //noinspection unchecked
            return (Map<String, Object>) field.get(template);
        } catch (ReflectiveOperationException e) {
            return new LinkedHashMap<>();
        }
    }

    private void clearParamsQuery(RequestTemplate template) {
        Map<String, Object> queries = getRawQueries(template);
        queries.remove("params");
    }
}
