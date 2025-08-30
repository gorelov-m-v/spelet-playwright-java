package tests;

import com.example.testsupport.TestApplication;
import com.example.testsupport.config.EnvironmentConfig;
import com.example.testsupport.framework.browser.PlaywrightManager;
import com.example.testsupport.framework.device.Device;
import com.example.testsupport.framework.listeners.PlaywrightExtension;
import com.example.testsupport.framework.localization.LocalizationService;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.example.testsupport.framework.utils.AllureHelper.step;

@SpringBootTest(classes = TestApplication.class)
@ExtendWith(PlaywrightExtension.class)
public abstract class BaseTest {

    @Autowired protected PlaywrightManager playwrightManager;
    @Autowired protected LocalizationService ls;
    @Autowired protected EnvironmentConfig environmentConfig;

    protected void setupTestEnvironment(Device device, String languageCode) {
        step("Устанавливаем размер окна просмотра", () -> {
            playwrightManager.getPage().setViewportSize(device.getWidth(), device.getHeight());
        });

        step("Устанавливаем язык теста", () -> {
            ls.loadLocale(languageCode);
        });
    }

    protected void checkConfig() {
        System.out.println("Base URL from config: " + environmentConfig.getApi().getBaseUrl());

        Assertions.assertNotNull(environmentConfig, "EnvironmentConfig is null!");
        Assertions.assertNotNull(environmentConfig.getTestDevices(), "env.testDevices is null!");

        List<Device> platforms = environmentConfig.getTestDevices().getPlatforms();
        Assertions.assertNotNull(platforms, "env.testDevices.platforms is null!");
        Assertions.assertFalse(platforms.isEmpty(), "env.testDevices.platforms is empty!");

        System.out.println("Found devices: " + platforms.size());
        System.out.println("First device: " + platforms.get(0).getName());
    }
}

