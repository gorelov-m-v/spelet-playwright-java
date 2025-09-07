package com.example.testsupport.framework.api.client.params;

import lombok.Builder;
import lombok.Getter;

/**
 * Query parameters for fetching gambling games.
 */
@Getter
@Builder
public class GamblingGamesParams {

    private String brandAliasArray;

    private String categoryAliasArray;

    private String search;

    private DeviceType deviceType;

    private Boolean showRestricted;

    private Integer page;

    private Integer perPage;
}
