package com.velsis.speedviolationservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "velsis.violation")
public record ViolationToleranceProperties(
    int toleranceMarginKmh,
    int toleranceMarginPercentage,
    int percentageThreshold
) {
    public ViolationToleranceProperties {
        if (toleranceMarginKmh < 0 || toleranceMarginPercentage < 0 || percentageThreshold < 0) {
            throw new IllegalStateException("Tolerance configuration values must not be negative");
        }
    }
}
