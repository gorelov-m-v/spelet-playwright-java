package com.example.testsupport.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Primary test configuration that loads {@link AppProperties} and {@link BrowserStackProperties}.
 */
@Configuration
@EnableConfigurationProperties({AppProperties.class, BrowserStackProperties.class})
public class TestConfig { }
