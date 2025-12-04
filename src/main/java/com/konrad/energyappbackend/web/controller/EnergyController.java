package com.konrad.energyappbackend.web.controller;

import com.konrad.energyappbackend.web.dto.ChargingWindowDto;
import com.konrad.energyappbackend.web.dto.DailyMixDto;
import com.konrad.energyappbackend.service.ChargingWindowService;
import com.konrad.energyappbackend.service.EnergyMixService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Energy API", description = "Endpoints for energy mix and charging window data")
public class EnergyController {

    private final EnergyMixService energyMixService;
    private final ChargingWindowService chargingWindowService;

    @GetMapping("/energy-mix")
    @Operation(summary = "Get daily energy mix for 3 days",
            description = "Returns energy generation mix for today, tomorrow and day after tomorrow")
    public List<DailyMixDto> getDailyMix(){
        return energyMixService.getDailyMixForThreeDays();
    }


    @GetMapping("/charging-window")
    @Operation(summary = "Get optimal charging window",
            description = "Returns the optimal time window for EV charging based on clean energy availability")
    public ChargingWindowDto getOptimalChargingWindow(@RequestParam @Min(1) @Max(6) int hours){
        return chargingWindowService.getOptimalWindow(hours);
    }

}
