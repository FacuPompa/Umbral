package com.umbral.web.controller;

import com.umbral.domain.dto.CreateGameSuggestionRequest;
import com.umbral.domain.dto.ExternalGameResponse;
import com.umbral.domain.dto.GameSuggestionResponse;
import com.umbral.domain.service.GameSuggestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/game-suggestions")
@Validated
public class GameSuggestionController {

    private final GameSuggestionService gameSuggestionService;

    public GameSuggestionController(GameSuggestionService gameSuggestionService) {
        this.gameSuggestionService = gameSuggestionService;
    }

    @GetMapping("/search")
    public List<ExternalGameResponse> search(
            @RequestParam @Size(min = 2, max = 100) String query
    ) {
        return gameSuggestionService.searchExternalGames(query);
    }

    @PostMapping
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public GameSuggestionResponse create(
            @Valid @RequestBody CreateGameSuggestionRequest request
    ) {
        return gameSuggestionService.createCurrentUserSuggestion(request);
    }
}
