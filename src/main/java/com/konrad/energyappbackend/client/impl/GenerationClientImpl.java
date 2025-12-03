package com.konrad.energyappbackend.client.impl;

import com.konrad.energyappbackend.client.GenerationClient;
import com.konrad.energyappbackend.client.dto.GenerationResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.ZonedDateTime;

@Service
public class GenerationClientImpl implements GenerationClient {

    private final WebClient webClient;

    public GenerationClientImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public GenerationResponse getGenerationInterval(ZonedDateTime from, ZonedDateTime to) {

        String fromStr = from.toInstant().toString();
        String toStr = to.toInstant().toString();

        return webClient.get()
                .uri(builder -> builder.path("/generation/{from}/{to}").build(fromStr, toStr))
                .retrieve()
                .bodyToMono(GenerationResponse.class)
                .block(Duration.ofSeconds(6));
    }

}
