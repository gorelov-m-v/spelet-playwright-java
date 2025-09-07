package com.example.testsupport.framework.api.dto.gambling;

import java.util.List;

/**
 * Response wrapper for the gambling games API.
 */
public record GamblingGamesResponse(
        int total,
        List<Game> games
) {}
