package com.example.testsupport.framework.api.dto.gambling;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Game(
    String id,
    String alias,
    String name,
    String image,
    String providerName,
    String ruleResource,
    boolean hasDemo,
    boolean canPlayDemo,
    Brand brand,
    List<String> labels
) {}
