package com.konrad.energyappbackend.web.controller;

import com.konrad.energyappbackend.exception.ExternalApiException;
import com.konrad.energyappbackend.service.ChargingWindowService;
import com.konrad.energyappbackend.service.EnergyMixService;
import com.konrad.energyappbackend.web.dto.ChargingWindowDto;
import com.konrad.energyappbackend.web.dto.DailyMixDto;
import com.konrad.energyappbackend.web.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for UK energy mix data and EV charging optimization.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Energy API", description = "UK Energy Mix and EV Charging Optimization")
public class EnergyController {

    private final EnergyMixService energyMixService;
    private final ChargingWindowService chargingWindowService;

    /**
     * Get aggregated energy mix for the next 3 days.
     */
    @GetMapping("/energy-mix")
    @Operation(summary = "Get energy mix for three days")
    public ResponseEntity<List<DailyMixDto>> getEnergyMix() {
        log.info("Fetching energy mix data");
        return ResponseEntity.ok(energyMixService.getDailyMixForThreeDays());
    }

    /**
     * Find optimal EV charging window based on clean energy availability in the next 2 days time period.
     */
    @GetMapping("/charging-window")
    @Operation(summary = "Find optimal EV charging window")
    public ResponseEntity<ChargingWindowDto> getOptimalChargingWindow(
            @Parameter(description = "Charging duration in hours (1-6)")
            @RequestParam(defaultValue = "3")
            @Min(value = 1, message = "Hours must be at least 1")
            @Max(value = 6, message = "Hours must be at most 6")
            int hours) {

        log.info("Finding optimal charging window for {} hours", hours);
        ChargingWindowDto result = chargingWindowService.getOptimalWindow(hours);
        log.info("Found window: {} to {} ({}% clean)",
                result.start(), result.end(), result.cleanEnergyShare());
        return ResponseEntity.ok(result);
    }

    // ========================================
    // Exception Handlers
    // ========================================

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(ExternalApiException ex) {
        log.error("External API error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(503, "External API Unavailable", ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation error: {}", message);
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(400, "Validation Failed", message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'",
                ex.getValue(), ex.getName());

        log.warn("Type mismatch: {}", message);
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(400, "Invalid Parameter", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal Server Error",
                        "An unexpected error occurred"));
    }
}