package com.velsis.speedviolationservice.dto;

import com.velsis.speedviolationservice.enums.ViolationSeverity;

public record ViolationSummary(
    ViolationSeverity severity,
    String ctbCode
) {
}
