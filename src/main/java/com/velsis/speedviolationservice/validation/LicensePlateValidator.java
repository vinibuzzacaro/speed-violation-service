package com.velsis.speedviolationservice.validation;

import java.util.regex.Pattern;

public final class LicensePlateValidator {
    static final Pattern OLD_FORMAT = Pattern.compile("^[A-Za-z]{3}[0-9]{4}$");
    static final Pattern MERCOSUL_FORMAT = Pattern.compile("^[A-Za-z]{3}[0-9][A-Za-z][0-9]{2}$");

    public static boolean isValid(String licensePlate) {
        if (licensePlate == null || licensePlate.isBlank()) {
            return false;
        }
        String plate = licensePlate.trim();
        return OLD_FORMAT.matcher(plate).matches() || MERCOSUL_FORMAT.matcher(plate).matches();
    }
}
