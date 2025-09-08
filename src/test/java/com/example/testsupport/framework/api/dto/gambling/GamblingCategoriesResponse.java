package com.example.testsupport.framework.api.dto.gambling;

import com.example.testsupport.framework.localization.LocalizationService;
import java.util.List;
import java.util.stream.Stream;

/**
 * Response wrapper for the gambling categories API.
 */
public record GamblingCategoriesResponse(List<GameCategory> gameCategories) {

    /**
     * Returns the titles of horizontal categories prepended with the localized lobby title.
     *
     * @param ls localization service for resolving the lobby title
     * @return list of horizontal category titles including lobby
     */
    public List<String> horizontalCategoryNames(LocalizationService ls) {
        String lobby = ls.get("casino.navigation.lobby");
        return Stream.concat(
                Stream.of(lobby),
                gameCategories.stream()
                        .filter(gc -> gc.type() == GameCategoryType.HORIZONTAL)
                        .map(GameCategory::name)
        ).toList();
    }

    /**
     * Returns only the titles of horizontal categories without the lobby item.
     *
     * @return list of horizontal category titles
     */
    public List<String> horizontalCategoryNames() {
        return gameCategories.stream()
                .filter(gc -> gc.type() == GameCategoryType.HORIZONTAL)
                .map(GameCategory::name)
                .toList();
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

