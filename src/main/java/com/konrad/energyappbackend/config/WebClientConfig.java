package com.konrad.energyappbackend.config;

import com.konrad.energyappbackend.exception.ExternalApiException;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for WebClient used to communicate with external APIs.
 * <p>
 * Configures:
 * - Connection timeouts (prevents hanging requests)
 * - Read/write timeouts (prevents slow response issues)
 * - Request/response logging (useful for debugging)
 *
 */
@Configuration
public class WebClientConfig {

    private static final int CONNECTION_TIMEOUT_MS = 5000; // 5 seconds
    private static final int READ_TIMEOUT_SECONDS = 5;
    private static final int WRITE_TIMEOUT_SECONDS = 5;

    @Bean
    public WebClient generationWebClient() {
        DefaultUriBuilderFactory factory =
                new DefaultUriBuilderFactory("https://api.carbonintensity.org.uk");
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECTION_TIMEOUT_MS)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)));

        return WebClient.builder()
                .uriBuilderFactory(factory)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(mapErrors())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            LoggerFactory.getLogger("WebClient").info("Request: {} {}", req.method(), req.url());
            return Mono.just(req);
        });
    }

    private ExchangeFilterFunction mapErrors() {
        return ExchangeFilterFunction.ofResponseProcessor(resp -> {
            if (resp.statusCode().is2xxSuccessful()) {
                return Mono.just(resp);
            }
            return resp.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .flatMap(body -> Mono.error(new ExternalApiException(
                            "Generation API error %s: %s".formatted(resp.statusCode(), body))));
        });
    }

}

