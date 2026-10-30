package com.pm.paymentservicenew.domain.model.wallet;

public enum WalletTransactionType {

    /*
     * Escrow -> Available Wallet
     */
    CREDIT,

    /*
     * Available -> Reserved
     */
    RESERVE,

    /*
     * Reserved -> Available
     */
    RESERVE_RELEASE,

    /*
     * Reserved -> External Bank
     */
    WITHDRAWAL,

    /*
     * Customer refund
     */
    REFUND,

    /*
     * Admin adjustments
     */
    ADJUSTMENT,

    /*
     * Materials released from escrow
     */
    MATERIAL_ADVANCE

}