package com.umbral.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CheckpointDoesNotBelongToGameException extends RuntimeException {

    public CheckpointDoesNotBelongToGameException(String message) {
        super(message);
    }
}
