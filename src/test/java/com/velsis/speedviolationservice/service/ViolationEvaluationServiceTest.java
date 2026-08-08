package com.velsis.speedviolationservice.service;

import com.velsis.speedviolationservice.config.ViolationToleranceProperties;
import com.velsis.speedviolationservice.enums.ViolationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ViolationEvaluationServiceTest {
    private ViolationEvaluationService service;

    @BeforeEach
    void setUp() {
        service = new ViolationEvaluationService(new ViolationToleranceProperties(7, 7, 100));
    }

    @Nested
    @DisplayName("Tolerancia sobre a velocidade considerada")
    class ConsideredSpeed {

        @Test
        @DisplayName("limite regulamentado <= 100: aplica tolerancia absoluta em km/h")
        void appliesAbsoluteToleranceWhenSpeedLimitAtOrBelowThreshold() {
            int considered = service.calculateConsideredSpeed(92, 60);
            assertThat(considered).isEqualTo(85);
        }

        @Test
        @DisplayName("limite regulamentado > 100: aplica tolerancia percentual, truncada")
        void appliesPercentageToleranceWhenSpeedLimitAboveThreshold() {
            int considered = service.calculateConsideredSpeed(150, 110);
            assertThat(considered).isEqualTo(139);
        }

        @Test
        @DisplayName("o limiar de 100km/h e avaliado sobre o limite regulamentado, nao a velocidade medida")
        void thresholdIsEvaluatedAgainstSpeedLimitNotMeasuredSpeed() {
            int considered = service.calculateConsideredSpeed(130, 80);
            assertThat(considered).isEqualTo(123); // 130 - 7, e nao 130 * 0.93 = 120
        }

        @Test
        @DisplayName("limite regulamentado exatamente 100 ainda usa tolerancia absoluta (ate 100 km/h)")
        void thresholdBoundaryUsesAbsoluteTolerance() {
            int considered = service.calculateConsideredSpeed(110, 100);
            assertThat(considered).isEqualTo(103);
        }
    }

    @Nested
    @DisplayName("Percentual de excesso e deteccao de infracao")
    class ExcessAndViolationDetection {

        @Test
        @DisplayName("velocidade medida igual ao limite -> sem infracao")
        void measuredSpeedEqualToLimit_noViolation() {
            var result = service.evaluate(60, 60);
            assertThat(result.hasViolation()).isFalse();
            assertThat(result.excessPercentage()).isEqualTo(0.0);
            assertThat(result.severity()).isNull();
        }

        @Test
        @DisplayName("velocidade medida abaixo do limite -> sem infracao")
        void measuredSpeedBelowLimit_noViolation() {
            var result = service.evaluate(50, 60);
            assertThat(result.hasViolation()).isFalse();
        }

        @Test
        @DisplayName("velocidade dentro da margem de tolerancia -> sem infracao")
        void withinToleranceMargin_noViolation() {
            var result = service.evaluate(64, 60);
            assertThat(result.hasViolation()).isFalse();
            assertThat(result.consideredSpeed()).isEqualTo(57);
            assertThat(result.excessPercentage()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("excesso calculado e arredondado para 2 casas decimais")
        void excessPercentageIsRoundedToTwoDecimals() {
            var result = service.evaluate(92, 60);
            assertThat(result.hasViolation()).isTrue();
            assertThat(result.consideredSpeed()).isEqualTo(85);
            assertThat(result.excessPercentage()).isEqualTo(41.67);
        }
    }

    @Nested
    @DisplayName("Classificacao de gravidade e valores de fronteira")
    class SeverityClassification {

        @ParameterizedTest(name = "excesso={0}% -> {1}")
        @CsvSource({
            "0.01, MEDIUM, 218-I",
            "20.0, MEDIUM, 218-I",
            "20.01, SERIOUS, 218-II",
            "50.0, SERIOUS, 218-II",
            "50.01, VERY_SERIOUS, 218-III",
            "150.0, VERY_SERIOUS, 218-III"
        })
        void classifiesSeverityByExcessPercentage(double excess, ViolationSeverity expectedSeverity, String expectedCode) {
            ViolationSeverity severity = ViolationSeverity.fromExcessPercentage(excess);
            assertThat(severity).isEqualTo(expectedSeverity);
            assertThat(severity.ctbCode()).isEqualTo(expectedCode);
        }

        @Test
        @DisplayName("fronteira exata de 20% de excesso via evaluate()")
        void boundaryAt20PercentViaEvaluate() {
            var result = service.evaluate(127, 100);
            assertThat(result.excessPercentage()).isEqualTo(20.0);
            assertThat(result.severity()).isEqualTo(ViolationSeverity.MEDIUM);
        }

        @Test
        @DisplayName("fronteira exata de 50% de excesso via evaluate()")
        void boundaryAt50PercentViaEvaluate() {
            var result = service.evaluate(157, 100);
            assertThat(result.excessPercentage()).isEqualTo(50.0);
            assertThat(result.severity()).isEqualTo(ViolationSeverity.SERIOUS);
        }
    }
}
