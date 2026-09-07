package com.umbral.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class HandleAlreadyInUseException extends RuntimeException {

    public HandleAlreadyInUseException() {
        super("Ese nombre de usuario ya está en uso.");
    }
}
