package com.example.testsupport.framework.api.client;

import com.example.testsupport.framework.allure.Suite;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Suite("FrontApiClient")
@DisplayName("FrontApiClientIntegration")
class FrontApiClientIntegrationTest {

    private static MockWebServer server;

    @DynamicPropertySource
    static void registerBaseUrl(DynamicPropertyRegistry registry) throws IOException {
        server = new MockWebServer();
        server.start();
        registry.add("env.api.base-url", () -> server.url("/").toString());
    }

    @AfterAll
    static void shutdown() throws IOException {
        server.shutdown();
    }

    @Autowired
    private FrontApiClient client;

    @Test
    @Tag("Unit-test")
    @DisplayName("Передает параметры в HTTP запросе")
    void passesParamsToHttpRequest() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"total\":0,\"games\":[]}"));

        client.getGamblingGames("brand-1");

        RecordedRequest request = server.takeRequest();
        assertEquals("/_front_api/api/v1/gambling/games?brandAliasArray=brand-1", request.getPath());
    }
}
