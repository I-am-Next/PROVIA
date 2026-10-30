package com.pm.notificationservice.api.error.response;

public record ApiResponse<T>(

        boolean success,

        T data,

        ApiError error

) {

    public static <T> ApiResponse<T> ok(

            T data

    ) {

        return new ApiResponse<>(

                true,

                data,

                null

        );

    }

    public static ApiResponse<Void> ok() {

        return new ApiResponse<>(

                true,

                null,

                null

        );

    }

    public static <T> ApiResponse<T> error(

            ApiError error

    ) {

        return new ApiResponse<>(

                false,

                null,

                error

        );

    }

}
