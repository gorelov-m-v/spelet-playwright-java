package com.example.testsupport.framework.api.client.params;

import lombok.Builder;
import lombok.Getter;

/**
 * Query parameters for fetching gambling brands.
 */
@Getter
@Builder
public class GamblingBrandsParams {

    private String deviceType;

    private Boolean showRestricted;

    private String categoryAlias;
}
