package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.framework.utils.Breakpoints;
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

    private final Page page;
    private final LocalizationService ls;
    private final Locator mobileLobbyButton;
    private final Locator desktopLobbyButton;

    public NavigationPanelComponent(Page page, LocalizationService ls) {
        super(page.locator("div.d_flex.gap_1.ov-x_auto.pos_relative").first());
        this.page = page;
        this.ls = ls;
        this.mobileLobbyButton = root().locator("div.navigationTab__root--size_sm.navigationTab__root--isIcon_true.navigationTab__root--isSelected_true");
        this.desktopLobbyButton = root().locator("div.navigationTab__root--size_md.navigationTab__root--isIcon_true.navigationTab__root--isSelected_true");
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
     * Verifies that the lobby button is visible for the current viewport width.
     *
     * @return current component instance
     */
    public NavigationPanelComponent verifyLobbyButton() {
        return step("[NavigationPanelComponent] Проверка кнопки 'Лобби'", () -> {
            Locator lobbyButton;
            int width = page.viewportSize() != null ? page.viewportSize().width : Integer.MAX_VALUE;
            if (width < Breakpoints.TABLET) {
                lobbyButton = mobileLobbyButton;
            } else {
                lobbyButton = desktopLobbyButton;
            }
            assertThat(lobbyButton).isVisible();
            return this;
        });
    }
}
