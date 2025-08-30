package com.example.testsupport.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Primary test configuration that registers base beans.
 */
@Configuration
@EnableFeignClients(basePackages = "com.example.testsupport.framework.api.client")
@Import(PageConfig.class)
public class TestConfig { }
