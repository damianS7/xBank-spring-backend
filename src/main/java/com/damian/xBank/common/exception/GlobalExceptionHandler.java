package com.damian.xBank.common.exception;

import com.damian.xBank.auth.exception.AuthenticationException;
import com.damian.xBank.common.http.response.ApiResponse;
import com.damian.xBank.customer.exception.CustomerException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Internal error server", HttpStatus.INTERNAL_SERVER_ERROR));
//    }
}