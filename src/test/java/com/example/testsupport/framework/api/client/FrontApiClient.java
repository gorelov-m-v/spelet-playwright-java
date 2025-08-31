package com.example.testsupport.framework.api.client;

import com.example.testsupport.framework.api.client.params.GamblingBrandsParams;
import com.example.testsupport.framework.api.dto.gambling.GamblingBrandsResponse;
import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "frontApiClient", url = "${env.api.base-url}", configuration = GenericParamsInterceptor.class)
public interface FrontApiClient {

    @GetMapping("/_front_api/api/v1/gambling/brands")
    GamblingBrandsResponse getGamblingBrands(@Param("params") GamblingBrandsParams params);
}

