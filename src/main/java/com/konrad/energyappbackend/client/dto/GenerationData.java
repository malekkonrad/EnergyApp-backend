package com.konrad.energyappbackend.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GenerationData(
        String from,
        String to,
        @JsonProperty("generationmix")
        List<FuelMix> generationMix
) { }
