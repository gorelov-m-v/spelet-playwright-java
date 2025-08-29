package tests;

import com.example.testsupport.TestApplication;
import com.example.testsupport.framework.browser.PlaywrightManager;
import com.example.testsupport.framework.device.Device;
import com.example.testsupport.framework.listeners.PlaywrightExtension;
import com.example.testsupport.framework.localization.LocalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.example.testsupport.framework.utils.AllureHelper.step;

@SpringBootTest(classes = TestApplication.class)
@ExtendWith(PlaywrightExtension.class)
public abstract class BaseTest {

    @Autowired protected PlaywrightManager playwrightManager;
    @Autowired protected LocalizationService ls;

    @BeforeEach
    protected void setupTestEnvironment(Device device, String languageCode) {
        step(String.format("Подготовка тестового окружения [Устройство: %s, Язык: %s]", device, languageCode), () -> {
            step("Устанавливаем размер окна просмотра", () -> {
                playwrightManager.getPage().setViewportSize(device.width(), device.height());
            });

            step("Устанавливаем язык теста", () -> {
                ls.loadLocale(languageCode);
            });
        });
    }
}

