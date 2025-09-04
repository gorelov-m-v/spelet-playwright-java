package com.example.testsupport.framework.api.client.params;

import com.example.testsupport.framework.api.client.annotations.RequestQueryParam;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GamblingGamesParams {

    @RequestQueryParam("brandAliasArray")
    private String brandAliasArray;

    @RequestQueryParam("categoryAliasArray")
    private String categoryAliasArray;

    @RequestQueryParam("search")
    private String search;

    @RequestQueryParam("deviceType")
    private String deviceType;

    @RequestQueryParam("showRestricted")
    private Boolean showRestricted;

    @RequestQueryParam("page")
    private Integer page;

    @RequestQueryParam("perPage")
    private Integer perPage;
}
