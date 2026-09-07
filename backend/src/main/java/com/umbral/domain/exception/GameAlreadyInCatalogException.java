package com.umbral.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class GameAlreadyInCatalogException extends RuntimeException {

    public GameAlreadyInCatalogException() {
        super("Ese juego ya está disponible en Umbral.");
    }
}
