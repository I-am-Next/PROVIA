package com.pm.paymentservicenew.api.controller;

import com.pm.paymentservicenew.api.response.ApiResponse;
import com.pm.paymentservicenew.application.dto.response.WalletResponse;
import com.pm.paymentservicenew.application.dto.response.WalletTransactionResponse;
import com.pm.paymentservicenew.application.service.WalletService;
import com.pm.paymentservicenew.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallet")
@PreAuthorize("hasRole('PROVIDER')")
public class WalletController {

    private static final Logger log = LoggerFactory.getLogger(WalletController.class);

    private final WalletService walletService;

    public WalletController(

            WalletService walletService

    ) {

        this.walletService = walletService;

    }

    // -------------------------
    // GET MY WALLET
    // -------------------------

    @GetMapping
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet() {

        UUID providerId = SecurityUtils.getCurrentUserId();

        log.info("========== WALLET REQUEST ==========");
        log.info("Provider ID: {}", providerId);

        WalletResponse response = walletService.getWallet(

                providerId

        );

        log.info("Wallet retrieved successfully.");
        log.info("====================================");

        return ResponseEntity.ok(

                ApiResponse.success(

                        response

                )

        );

    }

    // -------------------------
    // GET MY WALLET TRANSACTIONS
    // -------------------------

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<WalletTransactionResponse>>> getTransactions() {

        UUID providerId = SecurityUtils.getCurrentUserId();

        log.info("========== WALLET TRANSACTIONS REQUEST ==========");
        log.info("Provider ID: {}", providerId);

        List<WalletTransactionResponse> response = walletService.getTransactions(

                providerId

        );

        log.info("Retrieved {} wallet transaction(s).", response.size());
        log.info("=================================================");

        return ResponseEntity.ok(

                ApiResponse.success(

                        response

                )

        );

    }

}