package com.tuxlogic.shiftiq.platform.shared.interfaces.rest;

import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class MediaControllerTest {

    private StorageService storageService;
    private MediaController mediaController;

    @BeforeEach
    void setUp() {
        storageService = Mockito.mock(StorageService.class);
        mediaController = new MediaController(storageService);
    }

    @Test
    @DisplayName("uploadMedia returns 201 CREATED with url on valid file")
    void uploadMediaSuccess() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.png",
                "image/png",
                "png content".getBytes()
        );

        when(storageService.uploadFile(eq(file), eq("products")))
                .thenReturn("https://res.cloudinary.com/demo/image/upload/v1/shiftiq/products/photo.png");

        ResponseEntity<?> response = mediaController.uploadMedia(file, "products");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("url")).isEqualTo("https://res.cloudinary.com/demo/image/upload/v1/shiftiq/products/photo.png");
    }

    @Test
    @DisplayName("uploadMedia returns 400 BAD REQUEST when file is empty")
    void uploadMediaEmptyFileReturnsBadRequest() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        ResponseEntity<?> response = mediaController.uploadMedia(emptyFile, "uploads");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("File cannot be empty");
    }

    @Test
    @DisplayName("uploadMedia returns 400 BAD REQUEST when storageService throws IllegalArgumentException")
    void uploadMediaInvalidFileFormatReturnsBadRequest() {
        MockMultipartFile exeFile = new MockMultipartFile(
                "file",
                "file.exe",
                "application/x-msdownload",
                "binary content".getBytes()
        );

        when(storageService.uploadFile(any(), any()))
                .thenThrow(new IllegalArgumentException("Only image files are allowed."));

        ResponseEntity<?> response = mediaController.uploadMedia(exeFile, "uploads");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Only image files are allowed.");
    }
}
