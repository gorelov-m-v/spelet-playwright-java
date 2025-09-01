package com.example.testsupport.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.example.testsupport.framework.api.config.AllureFeignLoggerConfig;
import com.example.testsupport.framework.api.config.FeignParamsConfig;

/**
 * Primary test configuration that registers base beans.
 */
@Configuration
@EnableFeignClients(
        basePackages = "com.example.testsupport.framework.api.client",
        defaultConfiguration = {
                FeignParamsConfig.class,
                AllureFeignLoggerConfig.class
        }
)
@Import(PageConfig.class)
public class TestConfig { }
