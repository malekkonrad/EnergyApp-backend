package com.konrad.energyappbackend.service;

import com.konrad.energyappbackend.web.dto.ChargingWindowDto;

public interface ChargingWindowService {
    ChargingWindowDto getOptimalWindow(int hours);
}
