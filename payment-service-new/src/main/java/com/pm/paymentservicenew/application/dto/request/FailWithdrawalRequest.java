package com.pm.paymentservicenew.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FailWithdrawalRequest(

        @NotBlank(message = "Failure reason must not be blank.")
        @Size(
                max = 500,
                message = "Failure reason must not exceed 500 characters."
        )
        String reason

) {
}
