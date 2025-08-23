package com.example.testsupport.framework.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads an environment JSON file and adds its properties to the Spring Environment.
 */
public class JsonConfigEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private static final String CONFIG_PROPERTY_NAME = "env.config";
    private static final String JSON_PROPERTY_SOURCE_NAME = "jsonConfig";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String configName = environment.getProperty(CONFIG_PROPERTY_NAME);
        if (configName == null || configName.isEmpty()) {
            return;
        }

        String resourcePath = "/envs/" + configName + ".json";
        Resource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            throw new IllegalStateException("Environment config file not found: " + resourcePath);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> jsonProperties = mapper.readValue(
                    resource.getInputStream(), new TypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> springProperties = new HashMap<>();
            jsonProperties.forEach((key, value) -> {
                String kebabCaseKey = key.replaceAll("([a-z])([A-Z]+)", "$1-$2").toLowerCase();
                springProperties.put("app." + kebabCaseKey, value);
            });

            MapPropertySource propertySource = new MapPropertySource(JSON_PROPERTY_SOURCE_NAME, springProperties);
            environment.getPropertySources().addFirst(propertySource);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process environment config: " + resourcePath, e);
        }
    }
}
