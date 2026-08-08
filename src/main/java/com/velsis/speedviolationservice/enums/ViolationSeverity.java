package com.velsis.speedviolationservice.enums;

public enum ViolationSeverity {
    MEDIUM("218-I"),
    SERIOUS("218-II"),
    VERY_SERIOUS("218-III");

    private final String ctbCode;

    ViolationSeverity(String ctbCode) {
        this.ctbCode = ctbCode;
    }

    public String ctbCode() {
        return ctbCode;
    }

    public static ViolationSeverity fromExcessPercentage(double excessPercentage) {
        if (excessPercentage <= 20.0) {
            return MEDIUM;
        }
        if (excessPercentage > 50.0) {
            return VERY_SERIOUS;
        }
        return SERIOUS;
    }
}
