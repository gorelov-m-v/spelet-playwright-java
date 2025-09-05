package com.example.testsupport.framework.api.client.params;

import com.example.testsupport.framework.api.client.annotations.RequestQueryParam;
import lombok.Builder;
import lombok.Getter;

/**
 * Parameters for the games endpoint. Only the selected brand alias is currently supported.
 */
@Getter
@Builder
public class GamblingGamesParams {

    @RequestQueryParam("brandAliasArray")
    private final String brandAliasArray;
}
