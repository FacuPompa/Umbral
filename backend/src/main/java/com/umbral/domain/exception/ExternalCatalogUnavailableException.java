package com.umbral.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ExternalCatalogUnavailableException extends RuntimeException {

    public ExternalCatalogUnavailableException() {
        super("El catálogo externo no está disponible en este momento.");
    }
}
