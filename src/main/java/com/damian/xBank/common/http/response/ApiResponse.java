package com.damian.xBank.common.http.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String message;
    private T data;
    private HttpStatus status;

    public ApiResponse(String message, T data, HttpStatus status) {
        this.message = message;
        this.data = data;
        this.status = status;
    }

//    public static ApiResponse<T> build(String message, ) {
//        return new ApiResponse<>();
//    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(null, data, HttpStatus.OK);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data, HttpStatus.OK);
    }

    public static <T> ApiResponse<T> success(T data, HttpStatus status) {
        return new ApiResponse<>(null, data, status);
    }

    public static <T> ApiResponse<T> success(String message, HttpStatus status) {
        return new ApiResponse<>(message, null, status);
    }

    public static <T> ApiResponse<T> error(String message, HttpStatus status) {
        return new ApiResponse<>(message, null, status);
    }

    public T getData() {
        return data;
    }

    public int getHttpCode() {
        return this.status.value();
    }

    public String getMessage() {
        return this.message;
    }

}