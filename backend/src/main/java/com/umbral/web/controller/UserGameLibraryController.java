package com.umbral.web.controller;

import com.umbral.domain.dto.CreateGameInLibraryRequest;
import com.umbral.domain.dto.GameInLibraryResponse;
import com.umbral.domain.dto.UpdateGameInLibraryFavoriteRequest;
import com.umbral.domain.dto.UpdateGameInLibraryStatusRequest;
import com.umbral.domain.service.UserGameLibraryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/library")
public class UserGameLibraryController {

    private final UserGameLibraryService userGameLibraryService;

    public UserGameLibraryController(UserGameLibraryService userGameLibraryService) {
        this.userGameLibraryService = userGameLibraryService;
    }

    @GetMapping
    public ResponseEntity<List<GameInLibraryResponse>> getCurrentUserLibrary() {
        List<GameInLibraryResponse> library = userGameLibraryService.getCurrentUserLibrary();

        return ResponseEntity.ok(library);
    }

    @PostMapping
    public ResponseEntity<GameInLibraryResponse> addGameToLibrary (@Valid @RequestBody CreateGameInLibraryRequest request) {
        GameInLibraryResponse libraryEntry = userGameLibraryService.addCurrentUserGame(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEntry);
    }

    @PatchMapping("/{gameId}/status")
    public ResponseEntity<GameInLibraryResponse> updateGameStatus(@PathVariable Long gameId, @Valid @RequestBody UpdateGameInLibraryStatusRequest request) {
        GameInLibraryResponse libraryEntry = userGameLibraryService.updateCurrentUserGameStatus(gameId, request);

        return ResponseEntity.ok(libraryEntry);
    }

    @PatchMapping("/{gameId}/favorite")
    public ResponseEntity<GameInLibraryResponse> updateGameFavorite(@PathVariable Long gameId, @Valid @RequestBody UpdateGameInLibraryFavoriteRequest request) {
        GameInLibraryResponse libraryEntry = userGameLibraryService.updateCurrentUserGameFavorite(gameId, request);

        return ResponseEntity.ok(libraryEntry);
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> removeGameFromLibrary(@PathVariable Long gameId){
        userGameLibraryService.removeCurrentUserGame(gameId);
        return ResponseEntity.noContent().build();
    }

}
