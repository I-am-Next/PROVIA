package com.pm.paymentservicenew.api.controller;

import com.pm.paymentservicenew.api.response.ApiResponse;
import com.pm.paymentservicenew.application.dto.request.CreateWithdrawalRequest;
import com.pm.paymentservicenew.application.dto.response.WithdrawalResponse;
import com.pm.paymentservicenew.application.service.WithdrawalService;
import com.pm.paymentservicenew.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/withdrawals")
@PreAuthorize("hasRole('PROVIDER')")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    public WithdrawalController(

            WithdrawalService withdrawalService

    ) {

        this.withdrawalService = withdrawalService;

    }

    // -------------------------
    // REQUEST WITHDRAWAL
    // -------------------------

    @PostMapping
    public ResponseEntity<ApiResponse<WithdrawalResponse>> requestWithdrawal(

            @Valid
            @RequestBody
            CreateWithdrawalRequest request

    ) {

        UUID providerId = SecurityUtils.getCurrentUserId();

        WithdrawalResponse response = withdrawalService.requestWithdrawal(

                providerId,

                request

        );

        return ResponseEntity.status(

                HttpStatus.CREATED

        ).body(

                ApiResponse.success(

                        response,

                        "Withdrawal requested successfully."

                )

        );

    }

    // -------------------------
    // GET SINGLE WITHDRAWAL
    // -------------------------

    @GetMapping("/{withdrawalId}")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> getWithdrawal(

            @PathVariable
            UUID withdrawalId

    ) {

        UUID providerId = SecurityUtils.getCurrentUserId();

        WithdrawalResponse response = withdrawalService.getWithdrawal(

                providerId,

                withdrawalId

        );

        return ResponseEntity.ok(

                ApiResponse.success(

                        response

                )

        );

    }

    // -------------------------
    // GET MY WITHDRAWALS
    // -------------------------

    @GetMapping
    public ResponseEntity<ApiResponse<List<WithdrawalResponse>>> getWithdrawals() {

        UUID providerId = SecurityUtils.getCurrentUserId();

        List<WithdrawalResponse> response = withdrawalService.getWithdrawals(

                providerId

        );

        return ResponseEntity.ok(

                ApiResponse.success(

                        response

                )

        );

    }

}