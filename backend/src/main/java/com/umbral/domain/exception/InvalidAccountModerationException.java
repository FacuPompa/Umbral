package com.umbral.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidAccountModerationException extends RuntimeException {
    public InvalidAccountModerationException() {
        super("No se puede cambiar el estado de tu propia cuenta ni de una cuenta moderadora.");
    }
}
