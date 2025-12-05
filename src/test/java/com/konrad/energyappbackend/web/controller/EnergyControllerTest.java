package com.konrad.energyappbackend.web.controller;

import com.konrad.energyappbackend.exception.ExternalApiException;
import com.konrad.energyappbackend.service.ChargingWindowService;
import com.konrad.energyappbackend.service.EnergyMixService;
import com.konrad.energyappbackend.web.dto.ChargingWindowDto;
import com.konrad.energyappbackend.web.dto.DailyMixDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(EnergyController.class)
@DisplayName("EnergyController REST API Tests")
class EnergyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnergyMixService energyMixService;

    @MockitoBean
    private ChargingWindowService chargingWindowService;

    // ========================================
    // GET /api/energy-mix tests
    // ========================================

    @Test
    @DisplayName("GET /api/energy-mix should return 200 with energy mix data")
    void shouldReturnEnergyMixSuccessfully() throws Exception {
        // Given
        List<DailyMixDto> mockData = List.of(
                new DailyMixDto(
                        LocalDate.of(2025, 12, 5),
                        Map.of(
                                "biomass", 10.5,
                                "nuclear", 20.0,
                                "wind", 30.5,
                                "solar", 15.0,
                                "coal", 24.0
                        ),
                        76.0
                ),
                new DailyMixDto(
                        LocalDate.of(2025, 12, 6),
                        Map.of(
                                "biomass", 12.0,
                                "nuclear", 22.0,
                                "wind", 28.0,
                                "solar", 18.0,
                                "coal", 20.0
                        ),
                        80.0
                )
        );

        when(energyMixService.getDailyMixForThreeDays()).thenReturn(mockData);

        // When & Then
        mockMvc.perform(get("/api/energy-mix")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].date", is("2025-12-05")))
                .andExpect(jsonPath("$[0].cleanPercentage", is(76.0)))
                .andExpect(jsonPath("$[0].mix.biomass", is(10.5)))
                .andExpect(jsonPath("$[0].mix.nuclear", is(20.0)))
                .andExpect(jsonPath("$[0].mix.wind", is(30.5)))
                .andExpect(jsonPath("$[1].date", is("2025-12-06")))
                .andExpect(jsonPath("$[1].cleanPercentage", is(80.0)));
    }

    @Test
    @DisplayName("GET /api/energy-mix should return 503 when external API fails")
    void shouldReturn503WhenExternalApiFails() throws Exception {
        // Given
        when(energyMixService.getDailyMixForThreeDays())
                .thenThrow(new ExternalApiException("Carbon Intensity API is unavailable"));

        // When & Then
        mockMvc.perform(get("/api/energy-mix"))
                .andDo(print())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status", is(503)))
                .andExpect(jsonPath("$.error", is("External API Unavailable")))
                .andExpect(jsonPath("$.message", containsString("Carbon Intensity API")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /api/energy-mix should return empty list when no data available")
    void shouldReturnEmptyListWhenNoData() throws Exception {
        // Given
        when(energyMixService.getDailyMixForThreeDays()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/energy-mix"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ========================================
    // GET /api/charging-window tests
    // ========================================

    @Test
    @DisplayName("GET /api/charging-window should return 200 with optimal window")
    void shouldReturnOptimalChargingWindowSuccessfully() throws Exception {
        // Given
        ChargingWindowDto mockWindow = new ChargingWindowDto(
                ZonedDateTime.parse("2025-12-05T12:00:00Z"),
                ZonedDateTime.parse("2025-12-05T15:00:00Z"),
                85.5
        );

        when(chargingWindowService.getOptimalWindow(3)).thenReturn(mockWindow);

        // When & Then
        mockMvc.perform(get("/api/charging-window")
                        .param("hours", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.start", is("2025-12-05T12:00:00Z")))
                .andExpect(jsonPath("$.end", is("2025-12-05T15:00:00Z")))
                .andExpect(jsonPath("$.cleanEnergyShare", is(85.5)));
    }

    @Test
    @DisplayName("GET /api/charging-window should return 400 when hours < 1")
    void shouldReturn400WhenHoursTooLow() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/charging-window")
                        .param("hours", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.message", containsString("Hours must be at least 1")));
    }

    @Test
    @DisplayName("GET /api/charging-window should return 400 when hours > 6")
    void shouldReturn400WhenHoursTooHigh() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/charging-window")
                        .param("hours", "7"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.message", containsString("Hours must be at most 6")));
    }

    @Test
    @DisplayName("GET /api/charging-window should return 400 when hours is not a number")
    void shouldReturn400WhenHoursIsNotANumber() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/charging-window")
                        .param("hours", "abc"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/charging-window should return 400 when hours is negative")
    void shouldReturn400WhenHoursIsNegative() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/charging-window")
                        .param("hours", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Hours must be at least 1")));
    }

    @Test
    @DisplayName("GET /api/charging-window should work with all valid hours (1-6)")
    void shouldWorkWithAllValidHours() throws Exception {
        // Test all valid hour values
        for (int hours = 1; hours <= 6; hours++) {
            ChargingWindowDto mockWindow = new ChargingWindowDto(
                    ZonedDateTime.parse("2025-12-05T10:00:00Z"),
                    ZonedDateTime.parse("2025-12-05T10:00:00Z").plusHours(hours),
                    75.0 + hours
            );

            when(chargingWindowService.getOptimalWindow(hours)).thenReturn(mockWindow);

            mockMvc.perform(get("/api/charging-window")
                            .param("hours", String.valueOf(hours)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cleanEnergyShare", is(75.0 + hours)));
        }
    }

    @Test
    @DisplayName("GET /api/charging-window should return 503 when external API fails")
    void shouldReturn503WhenExternalApiFailsForChargingWindow() throws Exception {
        // Given
        when(chargingWindowService.getOptimalWindow(anyInt()))
                .thenThrow(new ExternalApiException("Failed to fetch generation data"));

        // When & Then
        mockMvc.perform(get("/api/charging-window")
                        .param("hours", "3"))
                .andDo(print())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status", is(503)))
                .andExpect(jsonPath("$.error", is("External API Unavailable")))
                .andExpect(jsonPath("$.message", containsString("Failed to fetch generation data")));
    }

    @Test
    @DisplayName("GET /api/charging-window should return 500 for unexpected errors")
    void shouldReturn500ForUnexpectedErrors() throws Exception {
        // Given
        when(chargingWindowService.getOptimalWindow(anyInt()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(get("/api/charging-window")
                        .param("hours", "3"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", containsString("An unexpected error occurred")));
    }

    // ========================================
    // CORS and Headers tests
    // ========================================

    @Test
    @DisplayName("Should accept requests with correct Content-Type")
    void shouldAcceptCorrectContentType() throws Exception {
        // Given
        when(energyMixService.getDailyMixForThreeDays()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/energy-mix")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}