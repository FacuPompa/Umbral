package com.umbral.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class GameAlreadyInLibraryException extends RuntimeException {
    public GameAlreadyInLibraryException() {
        super("Ese juego ya está en tu biblioteca");
    }
}
