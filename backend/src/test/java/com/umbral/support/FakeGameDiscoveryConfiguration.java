package com.umbral.support;

import com.umbral.domain.dto.ExternalGameResponse;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.service.GameDiscoveryClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.LocalDate;
import java.util.List;

@TestConfiguration(proxyBeanMethods = false)
public class FakeGameDiscoveryConfiguration {

    @Bean
    @Primary
    GameDiscoveryClient gameDiscoveryClient() {
        return new GameDiscoveryClient() {
            @Override
            public List<ExternalGameResponse> search(String query) {
                return List.of(
                        new ExternalGameResponse(
                                3498L,
                                "Persona 5 Royal",
                                LocalDate.of(2020, 3, 31),
                                "https://images.example.test/persona-5-royal.jpg"
                        ),
                        new ExternalGameResponse(
                                9999L,
                                "Persona 3 Reload",
                                LocalDate.of(2024, 2, 2),
                                "https://images.example.test/persona-3-reload.jpg"
                        )
                );
            }

            @Override
            public ExternalGameResponse findById(Long rawgGameId) {
                return search("test").stream()
                        .filter(game -> game.rawgGameId().equals(rawgGameId))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("El juego externo no fue encontrado."));
            }
        };
    }
}
