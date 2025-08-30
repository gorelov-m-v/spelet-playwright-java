package com.example.testsupport.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

/**
 * Allows Spring Boot to load configuration from JSON files.
 */
public class JsonPropertySourceLoader implements PropertySourceLoader {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String[] getFileExtensions() {
        return new String[] {"json"};
    }

    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
        if (!resource.exists()) {
            return List.of();
        }
        Map<String, Object> map = mapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        Map<String, Object> flattened = new LinkedHashMap<>();
        flattenMap("", map, flattened);
        return List.of(new MapPropertySource(name, flattened));
    }

    @SuppressWarnings("unchecked")
    private void flattenMap(String prefix, Map<String, Object> source, Map<String, Object> target) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> m) {
                flattenMap(key, (Map<String, Object>) m, target);
            } else if (value instanceof List<?> list) {
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    if (item instanceof Map<?, ?> itemMap) {
                        flattenMap(key + "[" + i + "]", (Map<String, Object>) itemMap, target);
                    } else {
                        target.put(key + "[" + i + "]", item);
                    }
                }
            } else {
                target.put(key, value);
            }
        }
    }
}
