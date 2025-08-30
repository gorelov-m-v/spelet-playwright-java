package com.example.testsupport.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Primary test configuration that loads {@link AppProperties} and {@link BrowserStackProperties}.
 */
@Configuration
@EnableConfigurationProperties({AppProperties.class, BrowserStackProperties.class})
@Import(PageConfig.class)
public class TestConfig { }
