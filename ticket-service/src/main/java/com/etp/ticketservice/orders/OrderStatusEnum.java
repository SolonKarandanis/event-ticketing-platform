package com.etp.ticketservice.orders;

public enum OrderStatusEnum {
    PENDING,
    PAID,
    EXPIRED,
    // Stripe took the money but the order can't be fulfilled -- PendingRefundSweeper
    // refunds it in full.
    REFUND_PENDING,
    REFUNDED,
    REFUND_FAILED
}
