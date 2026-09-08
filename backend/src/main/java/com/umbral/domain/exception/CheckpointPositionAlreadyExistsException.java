package com.umbral.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CheckpointPositionAlreadyExistsException extends RuntimeException {

    public CheckpointPositionAlreadyExistsException() {
        super("Ese juego ya tiene un checkpoint en esa posición.");
    }
}
