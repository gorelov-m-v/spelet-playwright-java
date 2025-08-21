package com.example.testsupport.ui.pages;

import com.example.testsupport.framework.routing.PagePath;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@PagePath("/casino")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CasinoPage {
    private final Page page;

    public CasinoPage(Page page) {
        this.page = page;
    }

    public Locator pageTitle() {
        return page.locator("h1.page-title");
    }
}
