package com.damian.xBank.common.exception;

import com.damian.xBank.auth.exception.*;
import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.account.exception.BankingAccountException;
import com.damian.xBank.banking.account.exception.BankingAccountInsufficientFundsException;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.banking.card.exception.BankingCardException;
import com.damian.xBank.banking.card.exception.BankingCardMaximumCardsPerAccountLimitReached;
import com.damian.xBank.banking.card.exception.BankingCardNotFoundException;
import com.damian.xBank.banking.transactions.exception.BankingTransactionAuthorizationException;
import com.damian.xBank.banking.transactions.exception.BankingTransactionException;
import com.damian.xBank.banking.transactions.exception.BankingTransactionNotFoundException;
import com.damian.xBank.common.utils.ApiResponse;
import com.damian.xBank.customer.exception.CustomerEmailTakenException;
import com.damian.xBank.customer.exception.CustomerException;
import com.damian.xBank.customer.exception.CustomerNotFoundException;
import com.damian.xBank.customer.profile.exception.ProfileAuthorizationException;
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

    @ExceptionHandler(
            {
                    AuthenticationException.class,
                    JwtAuthenticationException.class,
                    AuthenticationBadCredentialsException.class,
                    AccountDisabledException.class
            }
    )
    public ResponseEntity<ApiResponse<String>> handleUnauthorizedException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(
            {
                    EntityNotFoundException.class,
                    CustomerNotFoundException.class,
                    ProfileNotFoundException.class,
                    BankingAccountNotFoundException.class,
                    BankingCardNotFoundException.class,
                    BankingTransactionNotFoundException.class
            }
    )
    public ResponseEntity<ApiResponse<String>> handleNotFoundException(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(
            {
                    CustomerEmailTakenException.class,
                    BankingAccountInsufficientFundsException.class,
                    BankingCardMaximumCardsPerAccountLimitReached.class
            }
    )
    public ResponseEntity<ApiResponse<String>> handleConflitException(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(
            {
                    ApplicationException.class,
                    ProfileException.class,
                    BankingAccountException.class,
                    CustomerException.class,
                    BankingCardException.class,
                    BankingTransactionException.class
            }
    )
    public ResponseEntity<ApiResponse<String>> handleApplicationException(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(
            {
                    RuntimeException.class,
                    Exception.class
            }
    )
    public ResponseEntity<ApiResponse<String>> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(
            {
                    AuthorizationException.class,
                    ProfileAuthorizationException.class,
                    BankingCardAuthorizationException.class,
                    BankingAccountAuthorizationException.class,
                    BankingTransactionAuthorizationException.class,
                    BankingCardAuthorizationException.class,
                    PasswordMismatchException.class
            }
    )
    public ResponseEntity<ApiResponse<String>> handleAuthorizationException(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }
}