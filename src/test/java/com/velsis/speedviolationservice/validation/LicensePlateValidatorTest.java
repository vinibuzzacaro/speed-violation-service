package com.velsis.speedviolationservice.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LicensePlateValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"ABC1234", "abc1234", "XYZ9999"})
    void acceptsOldFormat(String plate) {
        assertThat(LicensePlateValidator.isValid(plate)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABC1D23", "abc1d23", "XYZ9A99"})
    void acceptsMercosulFormat(String plate) {
        assertThat(LicensePlateValidator.isValid(plate)).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "", " ", "ABC123", "ABC12345", "AB1234", "1234ABC", "ABCD123", "ABC1234X", "not-a-plate", "ABC1DD3"
    })
    void rejectsInvalidPlates(String plate) {
        assertThat(LicensePlateValidator.isValid(plate)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
        "' ABC1234 ', true",
        "'ABC1D23', true"
    })
    void trimsSurroundingWhitespaceBeforeValidating(String plate, boolean expected) {
        assertThat(LicensePlateValidator.isValid(plate)).isEqualTo(expected);
    }
}
