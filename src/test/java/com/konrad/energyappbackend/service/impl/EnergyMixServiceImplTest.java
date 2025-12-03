package com.konrad.energyappbackend.service.impl;

import com.konrad.energyappbackend.client.GenerationClient;
import com.konrad.energyappbackend.client.dto.FuelMix;
import com.konrad.energyappbackend.client.dto.GenerationData;
import com.konrad.energyappbackend.client.dto.GenerationResponse;
import com.konrad.energyappbackend.web.dto.DailyMixDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnergyMixService Tests")
class EnergyMixServiceImplTest {

    @Mock
    private GenerationClient generationClient;

    @InjectMocks
    private EnergyMixServiceImpl energyMixService;

    private GenerationResponse mockResponse;
    private LocalDate tomorrow;
    private LocalDate dayAfterTomorrow;
    private LocalDate threeDaysFromNow;

    @BeforeEach
    void setUp() {
        // Calculate dates based on current time (like the service does)
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        tomorrow = now.toLocalDate();
//        tomorrow = now.plusDays(1).toLocalDate();
        dayAfterTomorrow = now.plusDays(1).toLocalDate();
        threeDaysFromNow = now.plusDays(2).toLocalDate();



        // Create mock data with DYNAMIC dates
        List<GenerationData> dataList = new ArrayList<>();

        // Day 1 (tomorrow) - Morning slot
        dataList.add(createGenerationData(
                tomorrow.atTime(0, 0).atZone(ZoneId.of("UTC")),
                tomorrow.atTime(0, 30).atZone(ZoneId.of("UTC")),
                createRealisticFuelMix(10.0, 20.0, 2.0, 30.0, 5.0, 25.0, 6.0, 0.5, 1.5)
        ));

        // Day 1 (tomorrow) - Another slot
        dataList.add(createGenerationData(
                tomorrow.atTime(0, 30).atZone(ZoneId.of("UTC")),
                tomorrow.atTime(1, 0).atZone(ZoneId.of("UTC")),
                createRealisticFuelMix(12.0, 18.0, 3.0, 28.0, 6.0, 22.0, 8.0, 0.5, 2.5)
        ));

        // Day 2 (day after tomorrow) - Morning slot
        dataList.add(createGenerationData(
                dayAfterTomorrow.atTime(0, 0).atZone(ZoneId.of("UTC")),
                dayAfterTomorrow.atTime(0, 30).atZone(ZoneId.of("UTC")),
                createRealisticFuelMix(8.0, 22.0, 2.5, 35.0, 4.0, 20.0, 7.0, 0.5, 1.0)
        ));

        // Day 2 (day after tomorrow) - Another slot
        dataList.add(createGenerationData(
                dayAfterTomorrow.atTime(0, 30).atZone(ZoneId.of("UTC")),
                dayAfterTomorrow.atTime(1, 0).atZone(ZoneId.of("UTC")),
                createRealisticFuelMix(9.0, 21.0, 2.0, 32.0, 5.5, 22.0, 6.5, 0.5, 1.5)
        ));

        // Day 3 (three days from now) - Morning slot
        dataList.add(createGenerationData(
                threeDaysFromNow.atTime(0, 0).atZone(ZoneId.of("UTC")),
                threeDaysFromNow.atTime(0, 30).atZone(ZoneId.of("UTC")),
                createRealisticFuelMix(11.0, 19.0, 3.5, 33.0, 6.0, 18.0, 8.0, 0.5, 1.0)
        ));

        mockResponse = new GenerationResponse(dataList);
    }
    private List<FuelMix> createRealisticFuelMix(
            double biomass, double nuclear, double hydro, double wind, double solar,
            double coal, double gas, double imports, double other) {
        return List.of(
                new FuelMix("biomass", biomass),
                new FuelMix("nuclear", nuclear),
                new FuelMix("hydro", hydro),
                new FuelMix("wind", wind),
                new FuelMix("solar", solar),
                new FuelMix("coal", coal),
                new FuelMix("gas", gas),
                new FuelMix("imports", imports),
                new FuelMix("other", other)
        );
    }

    private GenerationData createGenerationData(ZonedDateTime from, ZonedDateTime to, List<FuelMix> generationMix) {
        String fromStr = from.toInstant().toString();
        String toStr = to.toInstant().toString();

        return new GenerationData(fromStr, toStr, generationMix);
    }

    @Test
    @DisplayName("Should return energy mix for three days")
    void shouldReturnEnergyMixForThreeDays() {
        // Given
        when(generationClient.getGenerationInterval(any(ZonedDateTime.class), any(ZonedDateTime.class)))
                .thenReturn(mockResponse);

        // When
        List<DailyMixDto> result = energyMixService.getDailyMixForThreeDays();

        // Then
        assertThat(result)
                .isNotNull()
                .hasSize(3);

        verify(generationClient, times(1))
                .getGenerationInterval(any(ZonedDateTime.class), any(ZonedDateTime.class));
    }

    @Test
    @DisplayName("Should calculate clean percentage correctly")
    void shouldCalculateCleanPercentageCorrectly() {
        // Given
        when(generationClient.getGenerationInterval(any(ZonedDateTime.class), any(ZonedDateTime.class)))
                .thenReturn(mockResponse);

        // When
        List<DailyMixDto> result = energyMixService.getDailyMixForThreeDays();

        // Then
        DailyMixDto firstDay = result.get(0);
        System.out.println(firstDay);


        assertThat(firstDay.cleanPercentage()).isEqualTo(67.0); // biomass(10) + nuclear(20) + wind(30)
    }

    @Test
    @DisplayName("Should group data by date")
    void shouldGroupDataByDate() {
        // Given
        when(generationClient.getGenerationInterval(any(ZonedDateTime.class), any(ZonedDateTime.class)))
                .thenReturn(mockResponse);

        // When
        List<DailyMixDto> result = energyMixService.getDailyMixForThreeDays();

        // Then
        assertThat(result)
                .extracting(DailyMixDto::date)
                .containsExactly(
                        LocalDate.parse(tomorrow.toString()),
                        LocalDate.parse(dayAfterTomorrow.toString()),
                        LocalDate.parse(threeDaysFromNow.toString())

                );
    }

    @Test
    @DisplayName("Should sort results by date")
    void shouldSortResultsByDate() {
        // Given
        when(generationClient.getGenerationInterval(any(ZonedDateTime.class), any(ZonedDateTime.class)))
                .thenReturn(mockResponse);

        // When
        List<DailyMixDto> result = energyMixService.getDailyMixForThreeDays();

        // Then
        assertThat(result)
                .extracting(DailyMixDto::date)
                .isSortedAccordingTo(LocalDate::compareTo);
    }

    private GenerationData createGenerationData(String from, String to, List<FuelMix> generationMix) {
        return new GenerationData(from, to, generationMix);
    }
}