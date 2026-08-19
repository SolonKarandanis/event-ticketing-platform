package com.etp.ticketservice.security;

public enum Role {
    ORGANIZER,
    ATTENDEE,
    STAFF;

    public static final String AUTHORITY_PREFIX = "ROLE_";

    public String getAuthority() {
        return AUTHORITY_PREFIX + name();
    }
}
