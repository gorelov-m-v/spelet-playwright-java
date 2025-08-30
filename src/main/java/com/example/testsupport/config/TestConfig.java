package com.example.testsupport.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Primary test configuration that loads {@link AppProperties} and {@link BrowserStackProperties}.
 */
@Configuration
@EnableConfigurationProperties({AppProperties.class, BrowserStackProperties.class})
@EnableFeignClients(basePackages = "com.example.testsupport.framework.api.client")
@Import(PageConfig.class)
public class TestConfig { }
