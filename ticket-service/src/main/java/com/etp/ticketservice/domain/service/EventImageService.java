package com.etp.ticketservice.domain.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

// Deliberately narrow -- everything this interface does is filesystem I/O and image
// processing (validate, resize, re-encode, read, delete), never a database call. It
// knows nothing about Event/EventImage entities; EventServiceImpl is the only caller,
// and owns the domainId each method is keyed by.
public interface EventImageService {

    // Validates (real type sniffing via a decode attempt, not just the declared
    // Content-Type header, plus a size cap), resizes, re-encodes to JPEG, and writes to
    // disk as "{imageDomainId}.jpg". Throws InvalidEventImageException on anything that
    // isn't a genuine, in-budget image.
    void storeImage(MultipartFile file, UUID imageDomainId);

    byte[] readImage(UUID imageDomainId);

    // No-op (logged, not thrown) if the file is already gone -- deleting something
    // that's not there isn't a real failure.
    void deleteImage(UUID imageDomainId);
}
