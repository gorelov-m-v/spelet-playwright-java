package com.example.testsupport.framework.device;

import com.example.testsupport.config.AppProperties;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/**
 * Service for building combinations of devices and languages for parameterized tests.
 * This class contains no JUnit dependencies and can be reused in different runners.
 */
@Service
public class TestMatrixService {

    private final DeviceConfiguration deviceConfig;
    private final AppProperties properties;

    public TestMatrixService(DeviceConfiguration deviceConfig, AppProperties properties) {
        this.deviceConfig = deviceConfig;
        this.properties = properties;
    }

    /**
     * Returns a stream of device/language combinations.
     */
    public Stream<List<Object>> getTestMatrix() {
        List<Device> allDevices = deviceConfig.getPlatforms();
        List<String> languages = properties.getLanguages();
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
                    .collect(Collectors.toMap(Device::name, d -> d));
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

