package com.velsis.speedviolationservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ViolationToleranceProperties.class)
public class ApplicationConfig {}
