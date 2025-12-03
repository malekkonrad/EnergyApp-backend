package com.konrad.energyappbackend.client;

import com.konrad.energyappbackend.client.dto.GenerationResponse;

import java.time.ZonedDateTime;


public interface GenerationClient {
    GenerationResponse getGenerationInterval(ZonedDateTime from, ZonedDateTime to);
}
