package com.velsis.speedviolationservice.controller;

import com.velsis.speedviolationservice.dto.EvaluateViolationRequest;
import com.velsis.speedviolationservice.dto.EvaluateViolationResponse;
import com.velsis.speedviolationservice.dto.ViolationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Violations")
public class ViolationController {

    @PostMapping("/v1/violations/evaluate")
    public ResponseEntity<EvaluateViolationResponse> evaluateV1(
        @Parameter(description = "Equipment origin: FIXED, MOBILE ou HANDHELD", required = true)
        @RequestHeader(value = "x-origin", required = false)
        String originHeader,

        @RequestBody EvaluateViolationRequest request
    ) {
        //TODO
        return null;
    }

    @GetMapping("/v1/violations")
    public ResponseEntity<List<ViolationResponse>> findByLicensePlateV1(
        @RequestParam("licensePlate") String licensePlate
    ) {
        //TODO
        return null;
    }
}
