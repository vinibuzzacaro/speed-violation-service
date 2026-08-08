package com.velsis.speedviolationservice.controller;

import com.velsis.speedviolationservice.dto.EvaluateViolationRequest;
import com.velsis.speedviolationservice.dto.EvaluateViolationResponse;
import com.velsis.speedviolationservice.dto.ViolationResponse;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public class ViolationController {

    @PostMapping("/evaluate")
    public ResponseEntity<EvaluateViolationResponse> evaluate(
        @Parameter(description = "Equipment origin: FIXED, MOBILE ou HANDHELD", required = true)
        @RequestHeader(value = "x-origin", required = false)
        String originHeader,

        @RequestBody EvaluateViolationRequest request
    ) {
        //TODO
        return null;
    }

    @GetMapping
    public ResponseEntity<List<ViolationResponse>> findByLicensePlate(
        @RequestParam("licensePlate") String licensePlate
    ) {
        //TODO
        return null;
    }
}
