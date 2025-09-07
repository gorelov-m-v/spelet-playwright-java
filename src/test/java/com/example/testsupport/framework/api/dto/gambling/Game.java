package com.example.testsupport.framework.api.dto.gambling;

import java.util.List;

/**
 * DTO representing a single gambling game.
 */
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
