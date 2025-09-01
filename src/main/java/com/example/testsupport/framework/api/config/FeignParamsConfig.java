package com.example.testsupport.framework.api.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that registers {@link com.example.testsupport.framework.api.client.GenericParamsInterceptor} as a Feign {@link RequestInterceptor}.
 */
@Configuration
public class FeignParamsConfig {

    @Bean
    public RequestInterceptor genericParamsInterceptor() {
        return new com.example.testsupport.framework.api.client.GenericParamsInterceptor();
    }
}

