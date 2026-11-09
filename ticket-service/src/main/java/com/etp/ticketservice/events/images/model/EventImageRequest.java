package com.etp.ticketservice.events.images.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventImageRequest {
    private UUID id;
    private Integer newImageIndex;
    private String altText;
}
