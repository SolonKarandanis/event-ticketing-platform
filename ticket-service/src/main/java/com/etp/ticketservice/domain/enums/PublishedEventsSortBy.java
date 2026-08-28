package com.etp.ticketservice.domain.enums;

public enum PublishedEventsSortBy {
    SOONEST,
    PRICE_ASC,
    PRICE_DESC,
    DISTANCE;

    // Maps the wire-level sortField value (e.g. "priceAsc", from
    // ListPublishedEventsRequestDto's inherited paging.sortField) onto its constant --
    // an absent or unrecognized value falls back to SOONEST, the same default the old
    // string-switch's "default" case used.
    public static PublishedEventsSortBy fromWireValue(String value) {
        if (null == value) {
            return SOONEST;
        }
        return switch (value) {
            case "priceAsc" -> PRICE_ASC;
            case "priceDesc" -> PRICE_DESC;
            case "distance" -> DISTANCE;
            default -> SOONEST;
        };
    }
}
