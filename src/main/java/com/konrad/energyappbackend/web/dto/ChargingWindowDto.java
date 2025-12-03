package com.konrad.energyappbackend.web.dto;

import java.time.ZonedDateTime;

public record ChargingWindowDto(ZonedDateTime start, ZonedDateTime end, double cleanEnergyShare) {
}
