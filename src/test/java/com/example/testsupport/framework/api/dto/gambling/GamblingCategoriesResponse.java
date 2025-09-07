package com.example.testsupport.framework.api.dto.gambling;

import java.util.List;

/**
 * Response wrapper for the gambling categories API.
 */
public record GamblingCategoriesResponse(List<GameCategory> gameCategories) {}

