package test.config;

import main.framework.browser.PlaywrightManager;
import com.microsoft.playwright.Page;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Configuration providing a prototype {@link Page} bean from PlaywrightManager.
 */
@Configuration
public class PageConfig {
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Page page(PlaywrightManager pm) {
        return pm.getPage();
    }
}
