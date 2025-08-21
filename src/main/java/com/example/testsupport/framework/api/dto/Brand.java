package com.example.testsupport.framework.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing a casino brand returned by the backend API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Brand {
    private String name;
    private String alias;

    public Brand() {
    }

    public Brand(String name, String alias) {
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
