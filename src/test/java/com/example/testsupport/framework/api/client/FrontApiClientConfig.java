package com.example.testsupport.framework.api.client;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FrontApiClientConfig {
    @Bean
    public RequestInterceptor genericParamsInterceptor() {
        return new GenericParamsInterceptor();
    }
}
