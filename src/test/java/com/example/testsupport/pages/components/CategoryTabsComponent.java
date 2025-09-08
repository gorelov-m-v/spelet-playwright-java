package com.example.testsupport.pages.components;

import com.microsoft.playwright.Page;
import java.util.List;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.example.testsupport.framework.utils.AllureHelper.step;

/**
 * Component representing the horizontal category tabs on the casino page.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CategoryTabsComponent extends BaseComponent {

    public CategoryTabsComponent(Page page) {
        super(page.locator("div.react-aria-Tabs[data-orientation='horizontal']").first());
    }

    /**
     * Returns a list of horizontal category titles displayed on the page.
     *
     * @return list of category titles in display order
     */
    public List<String> getTitles() {
        return step("Получение списка горизонтальных категорий", () ->
            root().locator("[role='tab']").allInnerTexts()
        );
    }
}
