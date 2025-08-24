package com.example.testsupport.framework.device;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Loads device definitions from {@code devices.yml}.
 */
@Component
@ConfigurationProperties(prefix = "test-devices")
@PropertySource(value = "classpath:devices.yml", factory = YamlPropertySourceFactory.class)
public class DeviceConfiguration {
    private List<Device> platforms;

    public List<Device> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<Device> platforms) {
        this.platforms = platforms;
    }
}
