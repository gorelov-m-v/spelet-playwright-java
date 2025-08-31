package com.example.testsupport.framework.api.client.params;

import com.example.testsupport.framework.api.client.annotations.RequestHeaderParam;
import com.example.testsupport.framework.api.client.annotations.RequestQueryParam;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GamblingBrandsParams {

    @RequestHeaderParam("Platform-NodeId")
    private String platformNodeId;

    @RequestHeaderParam("Platform-Locale")
    private String platformLocale;

    @RequestQueryParam("deviceType")
    private String deviceType;

    @RequestQueryParam("showRestricted")
    private Boolean showRestricted;

    @RequestQueryParam("categoryAlias")
    private String categoryAlias;
}
