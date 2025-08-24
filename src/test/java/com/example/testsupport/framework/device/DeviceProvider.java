package com.example.testsupport.framework.device;

import com.example.testsupport.config.AppProperties;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Provides combinations of devices and languages for parameterized tests.
 */
public class DeviceProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        ApplicationContext ctx = SpringExtension.getApplicationContext(context);
        DeviceConfiguration deviceConfig = ctx.getBean(DeviceConfiguration.class);
        AppProperties props = ctx.getBean(AppProperties.class);

        List<Device> allDevices = deviceConfig.getPlatforms();
        List<String> languages = props.getLanguages();
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
                        .map(lang -> Arguments.of(device, lang)));
    }
}
