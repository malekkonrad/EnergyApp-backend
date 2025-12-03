package com.konrad.energyappbackend.service;

import com.konrad.energyappbackend.web.dto.DailyMixDto;

import java.util.List;

public interface EnergyMixService {
    List<DailyMixDto> getDailyMixForThreeDays();
}
