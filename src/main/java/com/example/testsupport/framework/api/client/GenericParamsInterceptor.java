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
        if (!template.queries().containsKey("params")) {
            return;
        }

        Collection<?> paramsCollection = template.queries().get("params");
        if (paramsCollection == null || paramsCollection.isEmpty()) {
            clearParamsQuery(template);
            return;
        }

        Object params = paramsCollection.iterator().next();
        clearParamsQuery(template);

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

    private void clearParamsQuery(RequestTemplate template) {
        Map<String, Collection<String>> queries = new LinkedHashMap<>(template.queries());
        queries.remove("params");
        template.queries(queries);
        // Remove any body accidentally created for GET requests
        template.body((String) null);
    }
}
