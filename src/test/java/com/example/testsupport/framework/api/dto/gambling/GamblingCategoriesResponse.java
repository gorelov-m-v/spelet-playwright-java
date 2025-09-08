package com.example.testsupport.framework.api.dto.gambling;

import java.util.List;
import java.util.stream.Stream;

/**
 * Response wrapper for the gambling categories API.
 */
public record GamblingCategoriesResponse(List<GameCategory> gameCategories) {

    /**
     * Returns the titles of horizontal categories prepended with the localized lobby title.
     *
     * @param lobby localized lobby title
     * @return list of horizontal category titles including lobby
     */
    public List<String> horizontalCategoryNamesWithLobby(String lobby) {
        return Stream.concat(
                Stream.of(lobby),
                gameCategories.stream()
                        .filter(gc -> gc.type() == GameCategoryType.HORIZONTAL)
                        .map(GameCategory::name)
        ).toList();
    }

    /**
     * Returns the titles of navigation panel categories.
     *
     * @return list of navigation panel category titles
     */
    public List<String> navigationCategoryNames() {
        return gameCategories.stream()
                .filter(gc -> gc.type() == GameCategoryType.NAVIGATION_PANEL)
                .map(GameCategory::name)
                .toList();
    }
}

