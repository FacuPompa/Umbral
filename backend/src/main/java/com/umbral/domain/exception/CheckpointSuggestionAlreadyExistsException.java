package com.umbral.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CheckpointSuggestionAlreadyExistsException extends RuntimeException {

    public CheckpointSuggestionAlreadyExistsException() {
        super("Ya hay una propuesta pendiente para esa posición.");
    }
}
