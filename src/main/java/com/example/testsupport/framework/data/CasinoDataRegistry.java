package com.example.testsupport.framework.data;

import com.example.testsupport.framework.api.CasinoApiClient;
import com.example.testsupport.framework.api.dto.Brand;
import com.example.testsupport.framework.api.dto.Category;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Loads and caches casino-related data such as brands and categories.
 */
@Component
public class CasinoDataRegistry {
    private final CasinoApiClient apiClient;
    private Map<String, String> brandAliases = Collections.emptyMap();
    private Map<String, String> categoryAliases = Collections.emptyMap();

    public CasinoDataRegistry(CasinoApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @PostConstruct
    public void init() {
        brandAliases = apiClient.fetchBrands().stream()
                .collect(Collectors.toMap(Brand::getName, Brand::getAlias));
        categoryAliases = apiClient.fetchCategories().stream()
                .collect(Collectors.toMap(Category::getName, Category::getAlias));
    }

    public String getBrandAliasByName(String name) {
        return brandAliases.get(name);
    }

    public String getCategoryAliasByName(String name) {
        return categoryAliases.get(name);
    }
}
