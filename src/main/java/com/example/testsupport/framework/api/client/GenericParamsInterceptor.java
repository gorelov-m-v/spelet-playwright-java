package com.example.testsupport.framework.api.client;

import com.example.testsupport.framework.api.client.annotations.RequestHeaderParam;
import com.example.testsupport.framework.api.client.annotations.RequestQueryParam;
import feign.RequestInterceptor;
import feign.RequestTemplate;
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
        Map<String, Collection<?>> rawQueries = getRawQueries(template);
        if (!rawQueries.containsKey("params")) {
            return;
        }

        Collection<?> paramsCollection = rawQueries.get("params");
        if (paramsCollection == null || paramsCollection.isEmpty()) {
            clearParamsQuery(template, rawQueries);
            return;
        }

        Object params = paramsCollection.iterator().next();
        clearParamsQuery(template, rawQueries);

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

    private Map<String, Collection<?>> getRawQueries(RequestTemplate template) {
        try {
            Field field = RequestTemplate.class.getDeclaredField("queries");
            field.setAccessible(true);
            //noinspection unchecked
            return (Map<String, Collection<?>>) field.get(template);
        } catch (ReflectiveOperationException e) {
            return new LinkedHashMap<>();
        }
    }

    private void clearParamsQuery(RequestTemplate template, Map<String, Collection<?>> queries) {
        queries.remove("params");
        // cast to required type
        //noinspection unchecked
        template.queries((Map) new LinkedHashMap<>(queries));
    }
}
