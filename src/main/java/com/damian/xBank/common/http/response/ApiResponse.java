package com.damian.xBank.common.http.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String error;
    private String message;
    private T data;
    private HttpStatus status;

    public ApiResponse() {

    }

    public ApiResponse(String message, T data, HttpStatus status) {
        this.message = message;
        this.data = data;
        this.status = status;
    }

    public ApiResponse(String error) {
        this.error = error;
    }

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

    public static <T> ApiResponse<T> error(String error) {
        return new ApiResponse<>(error);
    }

    public static <T> ApiResponse<T> error(String error, T data, HttpStatus status) {
        ApiResponse response = new ApiResponse();
        response.setData(data);
        response.setError(error);
        response.setStatus(status);
        return response;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getHttpCode() {
        return this.status.value();
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return this.message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}