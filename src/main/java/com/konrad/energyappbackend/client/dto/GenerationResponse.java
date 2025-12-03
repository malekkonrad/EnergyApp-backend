package com.konrad.energyappbackend.client.dto;

import java.util.List;

public record GenerationResponse(
        List<GenerationData> data
) { }
