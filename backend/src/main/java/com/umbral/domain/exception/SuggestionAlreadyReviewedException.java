package com.umbral.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SuggestionAlreadyReviewedException extends RuntimeException {

    public SuggestionAlreadyReviewedException() {
        super("Esta sugerencia ya fue revisada.");
    }
}
