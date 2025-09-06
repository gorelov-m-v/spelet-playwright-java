package com.example.testsupport.framework.api.client;

import com.example.testsupport.framework.api.client.params.GamblingBrandsParams;
import com.example.testsupport.framework.api.dto.gambling.GamblingBrandsResponse;
import com.example.testsupport.framework.api.dto.gambling.GamblingGamesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "frontApiClient", url = "${env.api.base-url}", configuration = FrontApiClientConfig.class)
public interface FrontApiClient {

    @GetMapping("/_front_api/api/v1/gambling/brands")
    GamblingBrandsResponse getGamblingBrands(@RequestParam("params") GamblingBrandsParams params);

    @GetMapping("/_front_api/api/v1/gambling/games")
    ResponseEntity<GamblingGamesResponse> getGamblingGames(@RequestParam("brandAliasArray") String brandAlias);
}

