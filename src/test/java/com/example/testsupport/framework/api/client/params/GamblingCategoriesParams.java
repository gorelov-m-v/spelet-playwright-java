package com.example.testsupport.framework.api.client.params;

import lombok.Builder;
import lombok.Getter;


/**
 * Query parameters for fetching gambling categories.
 */
@Getter
@Builder
public class GamblingCategoriesParams {

    private DeviceType deviceType;

    private String categoryAliasArray;

    private Boolean showRestricted;
}

