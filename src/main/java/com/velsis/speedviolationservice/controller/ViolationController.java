package com.velsis.speedviolationservice.controller;

import com.velsis.speedviolationservice.dto.EvaluateViolationRequest;
import com.velsis.speedviolationservice.dto.EvaluateViolationResponse;
import com.velsis.speedviolationservice.dto.ViolationResponse;
import com.velsis.speedviolationservice.service.ViolationService;
import com.velsis.speedviolationservice.validation.EvaluationRequestValidator;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Tag(name = "Violations")
@RequiredArgsConstructor
public class ViolationController {
    private final ViolationService violationService;
    private final EvaluationRequestValidator requestValidator;

    @PostMapping("/v1/violations/evaluate")
    public ResponseEntity<EvaluateViolationResponse> evaluateV1(
        @Parameter(description = "Equipment origin: FIXED, MOBILE ou HANDHELD", required = true)
        @RequestHeader(value = "x-origin", required = false)
        String originHeader,

        @RequestBody EvaluateViolationRequest request
    ) {
        var validatedRequest = requestValidator.validate(originHeader, request);
        EvaluateViolationResponse response = violationService.evaluate(validatedRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/violations")
    public ResponseEntity<List<ViolationResponse>> findByLicensePlateV1(
        @RequestParam("licensePlate") String licensePlate
    ) {
        return ResponseEntity.ok(violationService.findByLicensePlate(licensePlate));
    }
}
