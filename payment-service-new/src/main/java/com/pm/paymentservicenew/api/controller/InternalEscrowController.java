package com.pm.paymentservicenew.api.controller;

import com.pm.paymentservicenew.application.dto.request.ReleaseMaterialsRequest;
import com.pm.paymentservicenew.application.dto.response.EscrowResponse;
import com.pm.paymentservicenew.application.service.EscrowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/escrows")
public class InternalEscrowController {

    private final EscrowService escrowService;

    public InternalEscrowController(EscrowService escrowService) {
        this.escrowService = escrowService;
    }

    @PostMapping("/{bookingId}/release-materials")
    public EscrowResponse releaseMaterials(
            @PathVariable UUID bookingId,
            @Valid @RequestBody ReleaseMaterialsRequest request
    ) {
        return escrowService.releaseMaterials(
                bookingId,
                request.quoteId(),
                request.amount()
        );
    }

    @PostMapping("/{bookingId}/release-labour")
    public EscrowResponse releaseLabour(
            @PathVariable UUID bookingId
    ) {
        return escrowService.releaseLabour(bookingId);
    }


    @PostMapping("/{bookingId}/refund")
    public ResponseEntity<Void> refundEscrow(
            @PathVariable UUID bookingId
    ) {
        escrowService.refundEscrow(bookingId);
        return ResponseEntity.noContent().build();
    }
}
