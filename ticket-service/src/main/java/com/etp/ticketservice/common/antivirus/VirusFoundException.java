package com.etp.ticketservice.common.antivirus;

public class VirusFoundException extends RuntimeException {
    private static final String defaultMessageKey = "error.content.viruses.found";

    public VirusFoundException() {
        super(defaultMessageKey);
    }

    public VirusFoundException(String msgKey) {
        super(msgKey);
    }

    public VirusFoundException(String msgKey, Throwable t) {
        super(msgKey, t);
    }
}
