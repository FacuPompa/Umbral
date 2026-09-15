package com.umbral.domain.service;

import com.umbral.domain.dto.GameResponse;
import com.umbral.domain.dto.PublicUserSearchResponse;
import com.umbral.domain.dto.SearchResponse;
import com.umbral.domain.entity.Game;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SearchService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public SearchService(GameRepository gameRepository, UserRepository userRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public SearchResponse search(String query) {
        String normalizedQuery = query == null ? "" : query.trim();

        if (normalizedQuery.length() < 2) {
            return new SearchResponse(List.of(), List.of());
        }

        List<GameResponse> games = gameRepository
                .findTop5ByTitleContainingIgnoreCaseOrderByTitleAsc(normalizedQuery)
                .stream()
                .map(this::toGameResponse)
                .toList();

        List<PublicUserSearchResponse> users = userRepository
                .findTop5ByHandleContainingIgnoreCaseOrderByHandleAsc(normalizedQuery)
                .stream()
                .map(user -> new PublicUserSearchResponse(user.getHandle()))
                .toList();

        return new SearchResponse(games, users);
    }

    private GameResponse toGameResponse(Game game) {
        return new GameResponse(
                game.getId(),
                game.getTitle(),
                game.getDescription(),
                game.getCoverImageUrl(),
                game.getReleaseDate()
        );
    }
}
