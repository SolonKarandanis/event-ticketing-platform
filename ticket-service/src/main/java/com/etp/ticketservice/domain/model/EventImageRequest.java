package com.etp.ticketservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventImageRequest {
    private UUID id;
    private Integer newImageIndex;
    private String altText;
}
