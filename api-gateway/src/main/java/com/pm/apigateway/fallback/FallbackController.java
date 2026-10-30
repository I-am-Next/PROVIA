package com.pm.apigateway.fallback;

import com.pm.apigateway.error.ApiResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {
    @RequestMapping("/fallback/booking")
    public ApiResponse<Object> bookingFallback() {
        return ApiResponse.error(
                "SERVICE_UNAVAILABLE",
                "Booking service is temporarily unavailable"
        );
    }
}
