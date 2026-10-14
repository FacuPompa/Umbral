package com.umbral.web.controller;

import com.umbral.domain.exception.InvalidPasswordChangeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.Map;

@RestControllerAdvice(assignableTypes = PasswordChangeController.class)
public class PasswordChangeExceptionHandler {
    @ExceptionHandler(InvalidPasswordChangeException.class)
    public ResponseEntity<Map<String, String>> invalidChange(InvalidPasswordChangeException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> invalidFields() {
        return ResponseEntity.badRequest().body(Map.of("message",
                "Revisá los campos. La nueva contraseña debe tener entre 8 y 72 caracteres."));
    }
}
