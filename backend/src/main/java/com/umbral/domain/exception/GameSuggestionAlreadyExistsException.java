package com.umbral.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class GameSuggestionAlreadyExistsException extends RuntimeException {

    public GameSuggestionAlreadyExistsException() {
        super("Ese juego ya fue enviado para revisión.");
    }
}
