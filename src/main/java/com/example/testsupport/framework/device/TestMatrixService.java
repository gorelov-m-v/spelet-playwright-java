package com.example.testsupport.framework.device;

import com.example.testsupport.config.EnvironmentConfig;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Function;
import org.springframework.stereotype.Service;

/**
 * Service for building combinations of devices and languages for parameterized tests.
 * This class contains no JUnit dependencies and can be reused in different runners.
 */
@Service
public class TestMatrixService {

    private final EnvironmentConfig config;

    public TestMatrixService(EnvironmentConfig config) {
        this.config = config;
    }

    /**
     * Returns a stream of device/language combinations.
     */
    public Stream<List<Object>> getTestMatrix() {
        List<Device> allDevices = config.getTestDevices().getPlatforms();
        if (allDevices == null || allDevices.isEmpty()) {
            throw new IllegalStateException("No test devices configured under env.test-devices.platforms");
        }
        List<String> languages = config.getBrowser().getLanguages();
        if (languages == null || languages.isEmpty()) {
            languages = List.of("lv", "ru", "en");
        }
        final List<String> finalLanguages = languages;

        String filter = System.getProperty("test.devices");
        List<Device> devices;
        if (filter == null || filter.isBlank()) {
            devices = allDevices;
        } else {
            Set<String> requested = Arrays.stream(filter.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
            Map<String, Device> deviceMap = allDevices.stream()
                    .collect(Collectors.toMap(Device::getName, Function.identity()));
            List<String> missing = requested.stream()
                    .filter(name -> !deviceMap.containsKey(name))
                    .toList();
            if (!missing.isEmpty()) {
                throw new IllegalArgumentException(
                        "Device(s) not found in configuration: " + String.join(", ", missing));
            }
            devices = requested.stream().map(deviceMap::get).toList();
        }

        return devices.stream()
                .flatMap(device -> finalLanguages.stream()
                        .map(lang -> List.of(device, lang)));
    }
}
