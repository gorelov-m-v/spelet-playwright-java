package com.example.testsupport.framework.api.dto.gambling;

/**
 * DTO describing a single gambling category.
 */
public record GameCategory(
        String id,
        String name,
        String alias,
        GameCategoryType type,
        int gamesCount,
        int sort,
        boolean isDefault
) {}

