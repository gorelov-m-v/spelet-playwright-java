package main.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Primary test configuration that loads {@link AppProperties}.
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class TestConfig { }
