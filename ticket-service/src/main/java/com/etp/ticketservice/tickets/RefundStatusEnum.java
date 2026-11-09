package com.etp.ticketservice.tickets;

public enum RefundStatusEnum {
    PENDING,
    SUCCEEDED,
    FAILED;

    public static RefundStatusEnum fromProviderStatus(String providerStatus) {
        return switch (providerStatus) {
            case "succeeded" -> SUCCEEDED;
            case "pending" -> PENDING;
            default -> FAILED; // "failed", "canceled", or any future value
        };
    }
}
