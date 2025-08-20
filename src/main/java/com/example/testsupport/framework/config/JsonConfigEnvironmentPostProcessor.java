package com.example.testsupport.framework.config;

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
 * Загружает JSON-файл окружения и добавляет его свойства в Spring Environment.
 */
public class JsonConfigEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private static final String CONFIG_PROPERTY = "env.config";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String configName = environment.getProperty(CONFIG_PROPERTY);
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
            EnvironmentConfig config = mapper.readValue(resource.getInputStream(), EnvironmentConfig.class);
            Map<String, Object> properties = new HashMap<>();
            properties.put("app.baseUrl", config.getBaseUrl());
            properties.put("app.defaultLanguage", config.getDefaultLanguage());
            properties.put("app.languages", config.getLanguages());
            MapPropertySource propertySource = new MapPropertySource("jsonConfig", properties);
            environment.getPropertySources().addFirst(propertySource);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read or parse environment config file: " + resourcePath, e);
        }
    }
}
