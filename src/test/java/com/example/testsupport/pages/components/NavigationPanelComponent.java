package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.List;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.example.testsupport.framework.utils.AllureHelper.step;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Component for the navigation panel on the casino page.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NavigationPanelComponent extends BaseComponent {

    private final LocalizationService ls;
    private final Locator lobbyButton;

    public NavigationPanelComponent(Page page, LocalizationService ls) {
        super(page.locator(
                "div.bg_navigationTab:has(div.navigationTab__root--isIcon_true)"
        ).first());
        this.ls = ls;
        this.lobbyButton = root().locator("div.navigationTab__root--isIcon_true").first();
    }

    /**
     * Returns a list of navigation panel category titles displayed on the page.
     *
     * @return list of navigation category titles in display order
     */
    public List<String> getTitles() {
        return step("[NavigationPanelComponent] Получение категорий навигационной панели", () -> {
            List<String> titles = root().locator("span.navigationTab__text").allInnerTexts();
            String providers = ls.get("casino.navigation.providers");
            return titles.stream()
                    .filter(title -> !title.equals(providers))
                    .toList();
        });
    }

    /**
     * Verifies that the lobby button is visible.
     *
     * @return current component instance
     */
    public NavigationPanelComponent verifyLobbyButton() {
        return step("[NavigationPanelComponent] Проверка кнопки 'Лобби'", () -> {
            assertThat(lobbyButton).isVisible();
            return this;
        });
    }
}
