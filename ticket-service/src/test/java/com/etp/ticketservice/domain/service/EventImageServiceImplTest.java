package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.exception.InvalidEventImageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Not a Mockito test -- EventImageServiceImpl has no repository/collaborator to mock, it
// does real file I/O and real image processing (Thumbnailator), so a real temp directory
// and real image bytes are what actually exercise its behavior. Constructed directly
// with a temp-dir path rather than through Spring's @Value injection, same as every
// other service test in this package skips the Spring context entirely.
class EventImageServiceImplTest {

    @TempDir
    private Path tempDir;

    private EventImageServiceImpl eventImageService;

    @BeforeEach
    void setUp() {
        eventImageService = new EventImageServiceImpl(tempDir.toString());
    }

    @Test
    void storeImage_emptyFile_throws() {
        MockMultipartFile empty = new MockMultipartFile("file", "cover.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> eventImageService.storeImage(empty, UUID.randomUUID()))
                .isInstanceOf(InvalidEventImageException.class);
    }

    @Test
    void storeImage_overFiveMegabytes_throws() {
        byte[] tooLarge = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile oversized = new MockMultipartFile("file", "cover.jpg", "image/jpeg", tooLarge);

        assertThatThrownBy(() -> eventImageService.storeImage(oversized, UUID.randomUUID()))
                .isInstanceOf(InvalidEventImageException.class);
    }

    @Test
    void storeImage_disallowedContentType_throws() {
        MockMultipartFile pdf = new MockMultipartFile("file", "doc.pdf", "application/pdf", "not an image".getBytes());

        assertThatThrownBy(() -> eventImageService.storeImage(pdf, UUID.randomUUID()))
                .isInstanceOf(InvalidEventImageException.class);
    }

    @Test
    void storeImage_missingContentType_throws() {
        MockMultipartFile noContentType = new MockMultipartFile("file", "cover.jpg", null, "bytes".getBytes());

        assertThatThrownBy(() -> eventImageService.storeImage(noContentType, UUID.randomUUID()))
                .isInstanceOf(InvalidEventImageException.class);
    }

    // A declared image/jpeg content type is only the fast-path check -- Thumbnailator
    // decoding the actual bytes is the real content-sniffing check (see the service's
    // own comment). Bytes that merely claim to be a JPEG without being one must still
    // be rejected.
    @Test
    void storeImage_declaredJpegButNotActuallyAnImage_throws() {
        MockMultipartFile spoofed = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "definitely not jpeg bytes".getBytes());

        assertThatThrownBy(() -> eventImageService.storeImage(spoofed, UUID.randomUUID()))
                .isInstanceOf(InvalidEventImageException.class);
    }

    @Test
    void storeImage_thenReadImage_roundTripsAsAValidJpeg() throws Exception {
        UUID imageId = UUID.randomUUID();
        MockMultipartFile realImage = new MockMultipartFile(
                "file", "cover.png", "image/png", realPngBytes(50, 50));

        eventImageService.storeImage(realImage, imageId);
        byte[] stored = eventImageService.readImage(imageId);

        assertThat(stored).isNotEmpty();
        // Re-encoded to JPEG on the way in (see EventImageServiceImpl's own comment) --
        // the stored bytes should decode as a real image regardless of the PNG upload.
        assertThat(ImageIO.read(new java.io.ByteArrayInputStream(stored))).isNotNull();
    }

    @Test
    void deleteImage_removesFile_soReadImageThenFailsAsServerInconsistency() throws Exception {
        UUID imageId = UUID.randomUUID();
        eventImageService.storeImage(
                new MockMultipartFile("file", "cover.png", "image/png", realPngBytes(20, 20)), imageId);

        eventImageService.deleteImage(imageId);

        assertThatThrownBy(() -> eventImageService.readImage(imageId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deleteImage_nonExistentFile_doesNotThrow() {
        eventImageService.deleteImage(UUID.randomUUID());
    }

    private byte[] realPngBytes(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
