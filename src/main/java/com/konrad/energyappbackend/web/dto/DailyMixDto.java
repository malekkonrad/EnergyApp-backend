package com.konrad.energyappbackend.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Map;

public record DailyMixDto(
        @NotNull LocalDate date,
        @NotNull Map<String, Double> mix,
        @Min(0) @Max(100) double cleanPercentage
) { }
