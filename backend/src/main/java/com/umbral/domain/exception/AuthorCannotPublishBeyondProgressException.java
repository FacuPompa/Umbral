package com.umbral.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AuthorCannotPublishBeyondProgressException extends RuntimeException {

    public AuthorCannotPublishBeyondProgressException(String message) {
        super(message);
    }
}
