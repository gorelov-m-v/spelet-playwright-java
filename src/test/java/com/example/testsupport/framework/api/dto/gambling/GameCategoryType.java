package com.example.testsupport.framework.api.dto.gambling;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Enumeration of category types returned by the gambling categories API.
 */
@Getter
public enum GameCategoryType {
    ALL_GAMES("allGames"),
    HORIZONTAL("horizontal"),
    VERTICAL("vertical"),
    NAVIGATION_PANEL("navigationPanel");

    private final String value;

    GameCategoryType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static GameCategoryType fromValue(String value) {
        for (GameCategoryType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown GameCategoryType value: " + value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

