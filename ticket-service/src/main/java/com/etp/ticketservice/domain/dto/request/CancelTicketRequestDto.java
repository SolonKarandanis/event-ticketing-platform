package com.etp.ticketservice.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// No reason field -- cancelReason (ATTENDEE_REQUEST/ORGANIZER_ACTION) is inferred
// server-side from which endpoint was called, not client-supplied. note is the only
// input either actor provides, and it's optional -- an empty/absent body is a valid
// cancel request with no note.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelTicketRequestDto {
    private String note;
}
