package com.example.testsupport.framework.api.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "frontApiClient", url = "${env.api.base-url}")
public interface FrontApiClient {
}

