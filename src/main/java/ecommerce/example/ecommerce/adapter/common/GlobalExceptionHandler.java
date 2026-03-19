package ecommerce.example.ecommerce.adapter.common;

import ecommerce.example.ecommerce.application.common.UseCaseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UseCaseException.class)
    public ResponseEntity<String> handleUseCaseException(UseCaseException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}