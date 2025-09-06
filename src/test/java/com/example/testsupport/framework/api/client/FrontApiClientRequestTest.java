package com.example.testsupport.framework.api.client;

import com.example.testsupport.framework.api.client.annotations.RequestHeaderParam;
import com.example.testsupport.framework.api.client.annotations.RequestQueryParam;
import com.example.testsupport.framework.api.client.params.GamblingBrandsParams;
import com.example.testsupport.framework.api.client.params.GamblingGamesParams;
import com.example.testsupport.framework.allure.Suite;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@Suite("Сборка HTTP-запросов")
@DisplayName("FrontApiClientRequest")
class FrontApiClientRequestTest {

    @Test
    @Tag("Unit-test")
    @DisplayName("Собирает запрос к API брендов из аннотированных полей")
    void buildsHttpRequestWithAnnotatedParams() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setResponseCode(200));
            server.start();

        GamblingBrandsParams params = GamblingBrandsParams.builder()
                .platformNodeId("node-1")
                .platformLocale("en-US")
                .deviceType("mobile")
                .showRestricted(true)
                .categoryAlias("slots")
                .build();

            HttpUrl.Builder urlBuilder = server.url("/_front_api/api/v1/gambling/brands").newBuilder();
            Headers.Builder headersBuilder = new Headers.Builder();

            for (Field field : GamblingBrandsParams.class.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(params);
                if (value == null) {
                    continue;
                }
                RequestQueryParam queryAnn = field.getAnnotation(RequestQueryParam.class);
                if (queryAnn != null) {
                    urlBuilder.addQueryParameter(queryAnn.value(), value.toString());
                }
                RequestHeaderParam headerAnn = field.getAnnotation(RequestHeaderParam.class);
                if (headerAnn != null) {
                    headersBuilder.add(headerAnn.value(), value.toString());
                }
            }

            Request request = new Request.Builder()
                .url(urlBuilder.build())
                .headers(headersBuilder.build())
                .build();

            new OkHttpClient().newCall(request).execute();

            RecordedRequest recorded = server.takeRequest();
            assertNotNull(recorded);
            HttpUrl url = recorded.getRequestUrl();
            assertEquals("mobile", url.queryParameter("deviceType"));
            assertEquals("true", url.queryParameter("showRestricted"));
            assertEquals("slots", url.queryParameter("categoryAlias"));
            assertEquals("node-1", recorded.getHeader("Platform-NodeId"));
            assertEquals("en-US", recorded.getHeader("Platform-Locale"));
        }
    }

    @Test
    @Tag("Unit-test")
    @DisplayName("Собирает запрос к API игр из аннотированных полей")
    void buildsGamesRequestWithAnnotatedParams() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setResponseCode(200));
            server.start();

            GamblingGamesParams params = GamblingGamesParams.builder()
                    .brandAliasArray("brand-1")
                    .build();

            HttpUrl.Builder urlBuilder = server.url("/_front_api/api/v1/gambling/games").newBuilder();
            Headers.Builder headersBuilder = new Headers.Builder();

            for (Field field : GamblingGamesParams.class.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(params);
                if (value == null) {
                    continue;
                }
                RequestQueryParam queryAnn = field.getAnnotation(RequestQueryParam.class);
                if (queryAnn != null) {
                    urlBuilder.addQueryParameter(queryAnn.value(), value.toString());
                }
            }

            Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .headers(headersBuilder.build())
                    .build();

            new OkHttpClient().newCall(request).execute();

            RecordedRequest recorded = server.takeRequest();
            assertNotNull(recorded);
            HttpUrl url = recorded.getRequestUrl();
            assertEquals("brand-1", url.queryParameter("brandAliasArray"));
        }
    }
}
