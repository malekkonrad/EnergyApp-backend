package com.konrad.energyappbackend.service.impl;

import com.konrad.energyappbackend.client.GenerationClient;
import com.konrad.energyappbackend.client.dto.GenerationData;
import com.konrad.energyappbackend.client.dto.GenerationResponse;
import com.konrad.energyappbackend.domain.EnergySource;
import com.konrad.energyappbackend.web.dto.ChargingWindowDto;
import com.konrad.energyappbackend.service.ChargingWindowService;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class ChargingWindowServiceImpl implements ChargingWindowService {

    private static final int INTERVALS_PER_HOUR = 2;
    private static final int MIN_HOURS = 1;
    private static final int MAX_HOURS = 6;

    private final GenerationClient generationClient;

    public ChargingWindowServiceImpl(GenerationClient generationClient) {
        this.generationClient = generationClient;
    }

    @Override
    public ChargingWindowDto getOptimalWindow(int hours) {
        if (hours < MIN_HOURS || hours > MAX_HOURS) {
            throw new IllegalArgumentException("Hours must be between 1 and 6");
        }

        ///  get time
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime from = now.toLocalDate().plusDays(1).atStartOfDay(ZoneId.of("UTC"));
        ZonedDateTime to = from.plusDays(2);

        ///  api call
        GenerationResponse response = generationClient.getGenerationInterval(from, to);
        List<GenerationData> dataList = response.data();

        ///  sliding window
        int windowSize = hours * INTERVALS_PER_HOUR;
        double maxCleanPercentage = 0.0;
        ZonedDateTime bestStart = null;
        ZonedDateTime bestEnd = null;

        ///  compute clean energy share per interval
        List<Double> cleanPercentageList = new ArrayList<>();
        for (GenerationData data : dataList) {
            cleanPercentageList.add(EnergySource.calculateCleanPercentage(data.generationMix()));
        }

        for (int i = 0; i <= cleanPercentageList.size() - windowSize; i++) {
            List<Double> window = cleanPercentageList.subList(i, i + windowSize);

            double cleanPercentage = window.stream().reduce(0.0, Double::sum);


            if (cleanPercentage > maxCleanPercentage) {
                maxCleanPercentage = cleanPercentage;
                bestStart = ZonedDateTime.parse(dataList.get(i).from());
                bestEnd = ZonedDateTime.parse(dataList.get(i + window.size() - 1).to());
            }
        }

        if (bestStart == null) {
            throw new RuntimeException("Could not find optimal charging window");
        }

        double averageCleanPercentage = maxCleanPercentage / windowSize;

        return new ChargingWindowDto(bestStart, bestEnd, averageCleanPercentage);
    }
}
