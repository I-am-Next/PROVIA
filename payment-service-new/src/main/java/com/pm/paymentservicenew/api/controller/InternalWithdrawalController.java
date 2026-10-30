package com.pm.paymentservicenew.api.controller;

import com.pm.paymentservicenew.api.response.ApiResponse;
import com.pm.paymentservicenew.application.dto.request.FailWithdrawalRequest;
import com.pm.paymentservicenew.application.dto.response.WithdrawalResponse;
import com.pm.paymentservicenew.application.service.WithdrawalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/withdrawals")
@Validated
public class InternalWithdrawalController {

    private final WithdrawalService withdrawalService;
    private final String internalServiceKey;

    public InternalWithdrawalController(
            WithdrawalService withdrawalService,
            @Value("${internal.service-key}") String internalServiceKey
    ) {
        this.withdrawalService = withdrawalService;
        this.internalServiceKey = internalServiceKey;
    }

    @PostMapping("/{withdrawalId}/process")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> startProcessing(
            @RequestHeader("X-Internal-Service-Key") String suppliedKey,
            @PathVariable UUID withdrawalId
    ) {
        verifyInternalKey(suppliedKey);

        WithdrawalResponse response =
                withdrawalService.startProcessingWithdrawal(withdrawalId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Withdrawal processing started successfully."
                )
        );
    }

    @PostMapping("/{withdrawalId}/complete")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> completeWithdrawal(
            @RequestHeader("X-Internal-Service-Key") String suppliedKey,
            @PathVariable UUID withdrawalId
    ) {
        verifyInternalKey(suppliedKey);

        WithdrawalResponse response =
                withdrawalService.completeWithdrawal(withdrawalId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Withdrawal completed successfully."
                )
        );
    }

    @PostMapping("/{withdrawalId}/fail")
    public ResponseEntity<Void> failWithdrawal(
            @RequestHeader("X-Internal-Service-Key") String suppliedKey,
            @PathVariable UUID withdrawalId,
            @Valid @RequestBody FailWithdrawalRequest request
    ) {
        verifyInternalKey(suppliedKey);

        withdrawalService.failWithdrawal(
                withdrawalId,
                request.reason()
        );

        return ResponseEntity.noContent().build();
    }

    private void verifyInternalKey(String suppliedKey) {
        if (!StringUtils.hasText(internalServiceKey)
                || !StringUtils.hasText(suppliedKey)
                || !MessageDigest.isEqual(
                internalServiceKey.getBytes(StandardCharsets.UTF_8),
                suppliedKey.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new InternalServiceUnauthorizedException();
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class InternalServiceUnauthorizedException
            extends RuntimeException {

        public InternalServiceUnauthorizedException() {
            super("Invalid internal service credentials.");
        }
    }
}
