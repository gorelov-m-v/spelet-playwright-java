package com.example.testsupport.framework.api.client;

import com.example.testsupport.framework.api.client.params.GamblingBrandsParams;
import com.example.testsupport.framework.api.client.params.GamblingCategoriesParams;
import com.example.testsupport.framework.api.dto.gambling.GamblingBrandsResponse;
import com.example.testsupport.framework.api.dto.gambling.GamblingCategoriesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "frontApiClient", url = "${env.api.base-url}")
public interface FrontApiClient {

    @GetMapping("/_front_api/api/v1/gambling/brands")
    GamblingBrandsResponse getGamblingBrands(
            @RequestHeader("Platform-Locale") String platformLocale,
            @SpringQueryMap GamblingBrandsParams params);

    @GetMapping("/_front_api/api/v2/gambling/categories")
    GamblingCategoriesResponse getGamblingCategories(
            @RequestHeader("Platform-Locale") String platformLocale,
            @SpringQueryMap GamblingCategoriesParams params);
}

