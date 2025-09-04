package com.example.testsupport.framework.api.client.params;

import com.example.testsupport.framework.api.client.annotations.RequestQueryParam;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GamblingGamesParams {

    @RequestQueryParam("brandAliasArray")
    private String brandAliasArray;
}
