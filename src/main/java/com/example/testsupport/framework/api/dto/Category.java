package com.example.testsupport.framework.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing a casino category returned by the backend API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {
    private String name;
    private String alias;

    public Category() {
    }

    public Category(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
