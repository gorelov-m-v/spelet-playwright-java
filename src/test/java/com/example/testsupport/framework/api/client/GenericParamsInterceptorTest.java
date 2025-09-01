package com.example.testsupport.framework.api.client;

import com.example.testsupport.TestApplication;
import com.example.testsupport.framework.api.client.params.GamblingBrandsParams;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = TestApplication.class)
class GenericParamsInterceptorTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private FrontApiClient frontApiClient;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("env.api.base-url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void paramsQueryIsRemoved() throws Exception {
        mockWebServer.enqueue(new MockResponse().setBody("{\"brands\":[]}")
                .addHeader("Content-Type", "application/json"));

        GamblingBrandsParams params = GamblingBrandsParams.builder()
                .platformLocale("lv")
                .categoryAlias("new")
                .build();

        frontApiClient.getGamblingBrands(params);

        RecordedRequest request = mockWebServer.takeRequest();
        assertFalse(request.getRequestUrl().queryParameterNames().contains("params"));
    }
}

