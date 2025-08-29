package test;

import main.TestApplication;
import main.framework.browser.PlaywrightManager;
import test.framework.device.Device;
import test.framework.listeners.PlaywrightExtension;
import main.framework.localization.LocalizationService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static test.framework.utils.AllureHelper.step;

@SpringBootTest(classes = TestApplication.class)
@ExtendWith(PlaywrightExtension.class)
public abstract class BaseTest {

    @Autowired protected PlaywrightManager playwrightManager;
    @Autowired protected LocalizationService ls;

    protected void setupTestEnvironment(Device device, String languageCode) {
        step("Устанавливаем размер окна просмотра", () -> {
            playwrightManager.getPage().setViewportSize(device.width(), device.height());
        });

        step("Устанавливаем язык теста", () -> {
            ls.loadLocale(languageCode);
        });
    }
}

