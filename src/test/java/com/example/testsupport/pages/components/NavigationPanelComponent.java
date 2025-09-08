package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Page;
import java.util.List;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.example.testsupport.framework.utils.AllureHelper.step;

/**
 * Component for the navigation panel on the casino page.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NavigationPanelComponent extends BaseComponent {

    private final LocalizationService ls;

    public NavigationPanelComponent(Page page, LocalizationService ls) {
        super(page.locator("div.d_flex.gap_1.ov-x_auto.pos_relative").first());
        this.ls = ls;
    }

    /**
     * Returns a list of navigation panel category titles displayed on the page.
     *
     * @return list of navigation category titles in display order
     */
    public List<String> getTitles() {
        return step("Получение категорий навигационной панели", () -> {
            List<String> titles = root().locator("span.navigationTab__text").allInnerTexts();
            String providers = ls.get("casino.navigation.providers");
            return titles.stream()
                    .filter(title -> !title.equals(providers))
                    .toList();
        });
    }
}
