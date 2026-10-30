package com.pm.providerservice.client.dto;

public record ApiResponse<T>(
        boolean success,
        T data,
        String error
) { }
