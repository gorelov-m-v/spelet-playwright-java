package com.example.testsupport.config;

import com.example.testsupport.base.PlaywrightManager;
import com.microsoft.playwright.Page;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Конфигурация, предоставляющая прототип-бин {@link Page} из PlaywrightManager.
 */
@Configuration
public class PageConfig {
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Page page(PlaywrightManager pm) {
        return pm.getPage();
    }
}
