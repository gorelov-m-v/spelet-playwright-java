package com.example.testsupport.framework.api.dto.gambling;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GamblingGamesResponse(
    int total,
    List<Game> games
) {}
