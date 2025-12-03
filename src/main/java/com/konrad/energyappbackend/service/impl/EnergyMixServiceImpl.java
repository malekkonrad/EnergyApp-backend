package com.konrad.energyappbackend.service.impl;

import com.konrad.energyappbackend.client.GenerationClient;
import com.konrad.energyappbackend.client.dto.FuelMix;
import com.konrad.energyappbackend.client.dto.GenerationData;
import com.konrad.energyappbackend.client.dto.GenerationResponse;
import com.konrad.energyappbackend.domain.EnergySource;
import com.konrad.energyappbackend.web.dto.DailyMixDto;
import com.konrad.energyappbackend.service.EnergyMixService;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnergyMixServiceImpl implements EnergyMixService {

    private static final int FORECAST_DAYS = 3; // today, tomorrow, day after tomorrow

    private final GenerationClient generationClient;

    public EnergyMixServiceImpl(GenerationClient generationClient) {
        this.generationClient = generationClient;
    }

    @Override
    public List<DailyMixDto> getDailyMixForThreeDays()
    {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime from = now.toLocalDate().atStartOfDay(ZoneId.of("UTC"));
        ZonedDateTime to = from.plusDays(FORECAST_DAYS);

        /// date helpers for three days
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);

        /// api call
        GenerationResponse response = generationClient.getGenerationInterval(from, to);

        /// group by day
        Map<LocalDate, List<GenerationData>> resultsByDay = response.data().stream()
                .collect(Collectors.groupingBy(this::dayOfInterval));

        List<LocalDate> days = List.of(today, tomorrow, dayAfterTomorrow);

        /// return correct DailyMixDto
        return days.stream()
                .map(day -> toDailyMix(day, resultsByDay.getOrDefault(day, List.of()))).toList();
    }

    private LocalDate dayOfInterval(GenerationData data) {
        return OffsetDateTime.parse(data.from()).toLocalDate();
    }

    private DailyMixDto toDailyMix(LocalDate date, List<GenerationData> intervals) {
        if (intervals.isEmpty()) {
            return new DailyMixDto(date, Map.of(), 0.0);
        }

        Map<String, Double> sums = new HashMap<>();

        for (GenerationData interval : intervals) {
            for (FuelMix mix: interval.generationMix()){
                sums.merge(mix.fuel(), mix.perc(), Double::sum );
            }
        }

        int numberOfIntervals = intervals.size();

        ///  Percentages:
        Map<String, Double> percentageUsage = sums.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() / numberOfIntervals));

        ///  Clean energy:
        double cleanEnergy = EnergySource.calculateCleanPercentage(percentageUsage);

        return new DailyMixDto(date, percentageUsage, cleanEnergy);
    }

}
