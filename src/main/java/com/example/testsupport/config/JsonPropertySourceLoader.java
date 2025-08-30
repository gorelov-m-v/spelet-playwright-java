package com.example.testsupport.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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
        return List.of(new MapPropertySource(name, map));
    }
}
