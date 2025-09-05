package com.example.testsupport.framework.api.client;

import com.example.testsupport.framework.api.client.params.GamblingGamesParams;
import feign.Feign;
import feign.mock.HttpMethod;
import feign.mock.MockClient;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class FrontApiClientTest {

    @Test
    void brandAliasParamIsSentAsQuery() {
        MockClient mockClient = new MockClient()
                .ok(HttpMethod.GET,
                        "/_front_api/api/v1/gambling/games?brandAliasArray=test",
                        "{}");

        FrontApiClient client = Feign.builder()
                .client(mockClient)
                .decoder((resp, type) -> ResponseEntity.status(resp.status()).build())
                .contract(new SpringMvcContract())
                .requestInterceptor(new GenericParamsInterceptor())
                .target(FrontApiClient.class, "http://localhost");

        GamblingGamesParams params = GamblingGamesParams.builder()
                .brandAliasArray("test")
                .build();

        assertDoesNotThrow(() -> client.getGamblingGames(params));
    }
}
