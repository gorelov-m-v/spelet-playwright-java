package com.example.testsupport.framework.api;

import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.api.dto.Brand;
import com.example.testsupport.framework.api.dto.Category;
import java.util.Collections;
import java.util.List;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Simple HTTP client for retrieving casino data used in routing.
 */
@Component
public class CasinoApiClient {
    private final RestTemplate restTemplate;
    private final AppProperties props;

    public CasinoApiClient(RestTemplateBuilder builder, AppProperties props) {
        this.restTemplate = builder.build();
        this.props = props;
    }

    private String apiBaseUrl() {
        // In real life this would be a dedicated API host
        return props.getBaseUrl() + "/api";
    }

    public List<Brand> fetchBrands() {
        try {
            ResponseEntity<List<Brand>> response = restTemplate.exchange(
                    apiBaseUrl() + "/brands",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Category> fetchCategories() {
        try {
            ResponseEntity<List<Category>> response = restTemplate.exchange(
                    apiBaseUrl() + "/categories",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
