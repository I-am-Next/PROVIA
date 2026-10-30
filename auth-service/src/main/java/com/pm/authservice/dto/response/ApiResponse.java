package com.pm.authservice.dto.response;

import java.time.LocalDateTime;

public class ApiResponse<T>{

    private boolean success;
    private T data;
    private Object error;
    private LocalDateTime timestamp;

    public ApiResponse(boolean success, T data, Object error, LocalDateTime timestamp) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(Object error) {
        return new ApiResponse<>(false, null, error, LocalDateTime.now());
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public Object getError() { return error; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
