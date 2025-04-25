package com.damian.xBank.common.exception;

import com.damian.xBank.auth.exception.AuthenticationAccountDisabledException;
import com.damian.xBank.auth.exception.AuthenticationBadCredentialsException;
import com.damian.xBank.auth.exception.AuthenticationException;
import com.damian.xBank.auth.exception.AuthorizationException;
import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.account.exception.BankingAccountException;
import com.damian.xBank.banking.account.exception.BankingAccountInsufficientFundsException;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.common.http.ApiResponse;
import com.damian.xBank.customer.exception.CustomerEmailTakenException;
import com.damian.xBank.customer.exception.CustomerException;
import com.damian.xBank.customer.exception.CustomerNotFoundException;
import com.damian.xBank.customer.profile.exception.ProfileException;
import com.damian.xBank.customer.profile.exception.ProfileNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation error", errors, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler({
            AuthenticationException.class,
            AuthenticationBadCredentialsException.class,
            AuthenticationAccountDisabledException.class
    })
    public ResponseEntity<ApiResponse<String>> handleUnauthorizedException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler({
            EntityNotFoundException.class,
            CustomerNotFoundException.class,
            ProfileNotFoundException.class,
            BankingAccountNotFoundException.class
    })
    public ResponseEntity<ApiResponse<String>> handleNotFoundException(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler({
            CustomerEmailTakenException.class,
            BankingAccountInsufficientFundsException.class
    })
    public ResponseEntity<ApiResponse<String>> handleConflitException(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler({
            ApplicationException.class,
            ProfileException.class,
            BankingAccountException.class,
            CustomerException.class
    })
    public ResponseEntity<ApiResponse<String>> handleApplicationException(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler({
            RuntimeException.class,
            Exception.class
    })
    public ResponseEntity<ApiResponse<String>> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler({
            AuthorizationException.class,
            BankingAccountAuthorizationException.class
    })
    public ResponseEntity<ApiResponse<String>> handleAuthorizationException(AuthorizationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }
}