package com.etp.ticketservice.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// Every field here is optional, same as the @RequestParam(required = false) list it
// replaced -- this is a filter bag, not a resource to validate the shape of. Paging now
// comes from the inherited SearchRequestDTO.paging instead of a separate Pageable
// method param -- see PublishedEventController for how page/limit map onto it. There's
// no dedicated sortBy field any more either: paging.sortingColumn (JSON "sortField")
// carries the same "soonest"/"priceAsc"/"priceDesc"/"distance" values sortBy used to.
// sortOrder is unused here -- each of those four is a named, pre-fixed-direction sort,
// not a generic column+direction pair (distance only ever makes sense nearest-first).
@Getter
@Setter
public class ListPublishedEventsRequestDto extends SearchRequestDTO {
    private String q;
    private LocalDateTime from;
    private LocalDateTime to;
    private Double minPrice;
    private Double maxPrice;
    private String city;
    private Double latitude;
    private Double longitude;
    private Double radiusMeters;
}
