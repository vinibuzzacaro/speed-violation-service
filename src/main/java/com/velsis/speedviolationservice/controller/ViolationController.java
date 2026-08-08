package com.velsis.speedviolationservice.controller;

import com.velsis.speedviolationservice.dto.EvaluateViolationRequest;
import com.velsis.speedviolationservice.dto.EvaluateViolationResponse;
import com.velsis.speedviolationservice.dto.ViolationResponse;
import com.velsis.speedviolationservice.enums.OriginType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public class ViolationController {

    @PostMapping("/evaluate")
    public ResponseEntity<EvaluateViolationResponse> evaluate(
        @RequestHeader("x-origin") OriginType originHeader,
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
