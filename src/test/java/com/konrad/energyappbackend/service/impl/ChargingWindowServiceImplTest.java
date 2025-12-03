package com.konrad.energyappbackend.service.impl;

import com.konrad.energyappbackend.client.GenerationClient;
import com.konrad.energyappbackend.client.dto.FuelMix;
import com.konrad.energyappbackend.client.dto.GenerationData;
import com.konrad.energyappbackend.client.dto.GenerationResponse;
import com.konrad.energyappbackend.web.dto.ChargingWindowDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChargingWindowService Tests")
class ChargingWindowServiceImplTest {

    @Mock
    private GenerationClient generationClient;

    @InjectMocks
    private ChargingWindowServiceImpl chargingWindowService;

    private GenerationResponse mockResponse;

    @BeforeEach
    void setUp() {
        List<GenerationData> dataList = new ArrayList<>();

        // Create 48 slots (24 hours)
        for (int i = 0; i < 48; i++) {
            int hour = i / 2;
            int minute = (i % 2) * 30;

            double windPercentage = hour >= 10 && hour <= 14 ? 40.0 : 20.0;
            double solarPercentage = hour >= 10 && hour <= 14 ? 25.0 : 5.0;

            String from = String.format("2025-12-03T%02d:%02d:00Z", hour, minute);
            String to = String.format("2025-12-03T%02d:%02d:00Z",
                    minute == 30 ? hour + 1 : hour,
                    minute == 30 ? 0 : 30);

            dataList.add(new GenerationData(
                    from,
                    to,
                    List.of(
                            new FuelMix("biomass", 10.0),
                            new FuelMix("nuclear", 20.0),
                            new FuelMix("hydro", 5.0),
                            new FuelMix("wind", windPercentage),
                            new FuelMix("solar", solarPercentage),
                            new FuelMix("coal", 25.0 - (windPercentage - 20.0)),
                            new FuelMix("gas", 15.0 - (solarPercentage - 5.0)),
                            new FuelMix("imports", 5.0),
                            new FuelMix("other", 0.0)
                    )
            ));
        }

        mockResponse = new GenerationResponse(dataList);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6})
    @DisplayName("Should find optimal window for valid hours")
    void shouldFindOptimalWindowForValidHours(int hours) {
        when(generationClient.getGenerationInterval(any(ZonedDateTime.class), any(ZonedDateTime.class)))
                .thenReturn(mockResponse);

        ChargingWindowDto result = chargingWindowService.getOptimalWindow(hours);

        assertThat(result).isNotNull();
        assertThat(result.start()).isNotNull();
        assertThat(result.end()).isNotNull();
        assertThat(result.cleanEnergyShare()).isBetween(0.0, 100.0);

        Duration duration = Duration.between(result.start(), result.end());
        assertThat(duration.toHours()).isEqualTo(hours);

        verify(generationClient, times(1))
                .getGenerationInterval(any(ZonedDateTime.class), any(ZonedDateTime.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 7, 10, 100})
    @DisplayName("Should throw exception for invalid hours")
    void shouldThrowExceptionForInvalidHours(int hours) {
        assertThatThrownBy(() -> chargingWindowService.getOptimalWindow(hours))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Hours must be between 1 and 6");

        verifyNoInteractions(generationClient);
    }

    @Test
    @DisplayName("Should select window with highest clean percentage")
    void shouldSelectWindowWithHighestCleanPercentage() {
        when(generationClient.getGenerationInterval(any(ZonedDateTime.class), any(ZonedDateTime.class)))
                .thenReturn(mockResponse);

        ChargingWindowDto result = chargingWindowService.getOptimalWindow(3);

        assertThat(result.cleanEnergyShare()).isGreaterThan(0.0);
    }
}