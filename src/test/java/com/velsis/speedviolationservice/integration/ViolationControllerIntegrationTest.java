package com.velsis.speedviolationservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ViolationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String requestBody(String plate, int measuredSpeed, int speedLimit, String timestamp) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
            "licensePlate", plate,
            "measuredSpeed", measuredSpeed,
            "speedLimit", speedLimit,
            "equipmentId", "RAD-CWB-001",
            "captureTimestamp", timestamp
        ));
    }

    @Test
    void evaluate_returnsViolationDetails_whenSpeedExceedsLimit() throws Exception {
        String body = requestBody("ABC1D23", 92, 60, Instant.now().minusSeconds(30).toString());

        mockMvc.perform(post("/api/v1/violations/evaluate")
                .contextPath("/api")
                .header("x-origin", "FIXED")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hasViolation").value(true))
            .andExpect(jsonPath("$.consideredSpeed").value(85))
            .andExpect(jsonPath("$.excessPercentage").value(41.67))
            .andExpect(jsonPath("$.violation.severity").value("SERIOUS"))
            .andExpect(jsonPath("$.violation.ctbCode").value("218-II"));
    }

    @Test
    void evaluate_returnsNoViolation_whenWithinTolerance() throws Exception {
        String body = requestBody("ABC1D23", 64, 60, Instant.now().minusSeconds(30).toString());

        mockMvc.perform(post("/api/v1/violations/evaluate")
                .contextPath("/api")
                .header("x-origin", "FIXED")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hasViolation").value(false))
            .andExpect(jsonPath("$.violation").isEmpty())
            .andExpect(jsonPath("$.excessPercentage").value(0.0));
    }

    @Test
    void evaluate_returns400_whenLicensePlateIsInvalid() throws Exception {
        String body = requestBody("INVALID", 92, 60, Instant.now().toString());

        mockMvc.perform(post("/api/v1/violations/evaluate")
                .contextPath("/api")
                .header("x-origin", "FIXED")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INVALID_LICENSE_PLATE"));
    }

    @Test
    void evaluate_returns400_whenOriginHeaderIsMissing() throws Exception {
        String body = requestBody("ABC1D23", 92, 60, Instant.now().toString());

        mockMvc.perform(post("/api/v1/violations/evaluate")
                .contextPath("/api")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INVALID_ORIGIN"));
    }

    @Test
    void evaluate_returns400_whenCaptureTimestampIsInTheFuture() throws Exception {
        String future = Instant.now().plus(1, ChronoUnit.DAYS).toString();
        String body = requestBody("ABC1D23", 92, 60, future);

        mockMvc.perform(post("/api/v1/violations/evaluate")
                .contextPath("/api")
                .header("x-origin", "FIXED")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INVALID_CAPTURE_TIMESTAMP"));
    }

    @Test
    void findByLicensePlate_returnsEmptyList_whenNoViolationsExist() throws Exception {
        mockMvc.perform(get("/api/v1/violations")
                .contextPath("/api")
                .param("licensePlate", "NEVER999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findByLicensePlate_returnsPersistedViolation_afterEvaluation() throws Exception {
        String plate = "XYZ9A99";
        String body = requestBody(plate, 92, 60, Instant.now().minusSeconds(30).toString());

        mockMvc.perform(post("/api/v1/violations/evaluate")
            .contextPath("/api")
            .header("x-origin", "FIXED")
            .contentType("application/json")
            .content(body));

        mockMvc.perform(get("/api/v1/violations")
                .contextPath("/api")
                .param("licensePlate", plate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].licensePlate").value(plate))
            .andExpect(jsonPath("$[0].severity").value("SERIOUS"));
    }
}
