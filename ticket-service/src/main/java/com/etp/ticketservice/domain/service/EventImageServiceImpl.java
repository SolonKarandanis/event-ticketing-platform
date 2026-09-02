package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.exception.ErrorCode;
import com.etp.ticketservice.domain.exception.InvalidEventImageException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class EventImageServiceImpl implements EventImageService {

    // 1600px on the long edge is plenty for a full-bleed detail-page hero image while
    // keeping files small; Thumbnails#size fits the source within this box, preserving
    // aspect ratio, without upscaling anything already smaller.
    private static final int MAX_DIMENSION = 1600;
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Path storageDir;

    public EventImageServiceImpl(@Value("${app.event-images.storage-dir}") String storageDir) {
        this.storageDir = Path.of(storageDir);
    }

    @Override
    public void storeImage(MultipartFile file, UUID imageDomainId) {
        if (file.isEmpty()) {
            throw new InvalidEventImageException(ErrorCode.EVENT_IMAGE_INVALID_FILE, "file is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidEventImageException(ErrorCode.EVENT_IMAGE_INVALID_FILE, "file exceeds 5MB");
        }

        // A fast-path rejection on the declared header -- cheap, and catches an
        // obviously wrong upload (a PDF, say) before spending any I/O on it. It's not
        // the real check: a spoofed header (a renamed .exe claiming to be image/jpeg)
        // sails through this and is instead caught below, when Thumbnailator itself
        // fails to decode bytes that aren't actually a real image, regardless of what
        // the header claimed.
        String declaredContentType = file.getContentType();
        if (null == declaredContentType || !ALLOWED_CONTENT_TYPES.contains(declaredContentType)) {
            throw new InvalidEventImageException(ErrorCode.EVENT_IMAGE_INVALID_FILE, declaredContentType);
        }

        try {
            Files.createDirectories(storageDir);
            Path target = storageDir.resolve(imageDomainId + ".jpg");
            Thumbnails.of(file.getInputStream())
                    .size(MAX_DIMENSION, MAX_DIMENSION)
                    .outputFormat("jpg")
                    .toFile(target.toFile());
        } catch (IOException | IllegalArgumentException ex) {
            // IllegalArgumentException is what Thumbnailator throws for bytes it can't
            // actually decode as an image -- this is the real content-sniffing check,
            // not the declared-header one above.
            throw new InvalidEventImageException(ErrorCode.EVENT_IMAGE_INVALID_FILE, ex);
        }
    }

    @Override
    public byte[] readImage(UUID imageDomainId) {
        Path path = storageDir.resolve(imageDomainId + ".jpg");
        try {
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            // The caller only ever reaches here after confirming the EventImage row
            // exists in the database -- a missing file at this point is a genuine
            // server-side inconsistency (e.g. the disk was wiped independently of the
            // DB), not a normal "not found" a client can trigger, so this surfaces as a
            // 500 via the generic exception handler rather than a domain ErrorCode.
            throw new IllegalStateException("Event image file missing for " + imageDomainId, ex);
        }
    }

    @Override
    public void deleteImage(UUID imageDomainId) {
        Path path = storageDir.resolve(imageDomainId + ".jpg");
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            log.warn("Failed to delete event image file for {}", imageDomainId, ex);
        }
    }
}
