package com.umbral.domain.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.umbral.domain.dto.ExternalGameResponse;
import com.umbral.domain.exception.ExternalCatalogUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.List;

@Component
public class RawgGameDiscoveryClient implements GameDiscoveryClient {

    private final RestClient restClient;
    private final String apiKey;

    public RawgGameDiscoveryClient(
            @Value("${umbral.integrations.rawg.api-key:}") String apiKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.rawg.io/api")
                .build();
        this.apiKey = apiKey.trim();
    }

    @Override
    public List<ExternalGameResponse> search(String query) {
        requireConfiguredKey();

        try {
            RawgGamesResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/games")
                            .queryParam("key", apiKey)
                            .queryParam("search", query)
                            .queryParam("page_size", 8)
                            .build())
                    .retrieve()
                    .body(RawgGamesResponse.class);

            if (response == null || response.results() == null) {
                return List.of();
            }

            return response.results().stream()
                    .filter(game -> game.id() != null && game.name() != null && !game.name().isBlank())
                    .map(this::toExternalGame)
                    .toList();
        } catch (RestClientException exception) {
            throw new ExternalCatalogUnavailableException();
        }
    }

    @Override
    public ExternalGameResponse findById(Long rawgGameId) {
        requireConfiguredKey();

        try {
            RawgGameResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/games/{rawgGameId}")
                            .queryParam("key", apiKey)
                            .build(rawgGameId))
                    .retrieve()
                    .body(RawgGameResponse.class);

            if (response == null || response.id() == null || response.name() == null || response.name().isBlank()) {
                throw new ExternalCatalogUnavailableException();
            }

            return toExternalGame(response);
        } catch (RestClientException exception) {
            throw new ExternalCatalogUnavailableException();
        }
    }

    private void requireConfiguredKey() {
        if (apiKey.isBlank()) {
            throw new ExternalCatalogUnavailableException();
        }
    }

    private ExternalGameResponse toExternalGame(RawgGameResponse game) {
        return new ExternalGameResponse(
                game.id(),
                game.name(),
                game.released(),
                game.backgroundImage()
        );
    }

    private record RawgGamesResponse(List<RawgGameResponse> results) {
    }

    private record RawgGameResponse(
            Long id,
            String name,
            LocalDate released,
            @JsonProperty("background_image") String backgroundImage
    ) {
    }
}
