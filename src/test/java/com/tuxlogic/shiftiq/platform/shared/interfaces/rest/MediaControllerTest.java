package com.tuxlogic.shiftiq.platform.shared.interfaces.rest;

import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.storage.StorageService;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(boolean isStaff) {
        var principal = Mockito.mock(AuthenticatedPrincipal.class);
        Mockito.lenient().when(principal.getId()).thenReturn(UUID.randomUUID());
        Mockito.lenient().when(principal.hasRole("ROLE_ADMIN")).thenReturn(isStaff);
        Mockito.lenient().when(principal.hasRole("ROLE_OWNER")).thenReturn(false);
        Mockito.lenient().when(principal.hasRole("ROLE_BRANCH_MANAGER")).thenReturn(false);
        Mockito.lenient().when(principal.hasRole("ROLE_EMPLOYEE")).thenReturn(false);
        Mockito.lenient().when(principal.hasRole("ROLE_ASSISTANT")).thenReturn(false);
        Mockito.lenient().when(principal.hasRole("ROLE_USER")).thenReturn(!isStaff);
        Mockito.lenient().when(principal.getBranchIds()).thenReturn(Set.of());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );
    }

    @Test
    @DisplayName("uploadMedia returns 201 CREATED with url when staff uploads valid file to operational folder")
    void uploadMediaSuccessForStaff() {
        authenticate(true);
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
    @DisplayName("uploadMedia returns 403 FORBIDDEN when customer uploads to operational folder")
    void uploadMediaForbiddenForCustomerOnOperationalFolder() {
        authenticate(false);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.png",
                "image/png",
                "png content".getBytes()
        );

        ResponseEntity<?> response = mediaController.uploadMedia(file, "products");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error").toString()).contains("Only workshop staff");
    }

    @Test
    @DisplayName("uploadMedia returns 201 CREATED when customer uploads to customer profile folder")
    void uploadMediaAllowedForCustomerOnCustomerFolder() {
        authenticate(false);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "png content".getBytes()
        );

        when(storageService.uploadFile(eq(file), eq("customers")))
                .thenReturn("https://res.cloudinary.com/demo/image/upload/v1/shiftiq/customers/avatar.png");

        ResponseEntity<?> response = mediaController.uploadMedia(file, "customers");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("url")).isEqualTo("https://res.cloudinary.com/demo/image/upload/v1/shiftiq/customers/avatar.png");
    }

    @Test
    @DisplayName("uploadMedia returns 400 BAD REQUEST when folder is disallowed")
    void uploadMediaDisallowedFolderReturnsBadRequest() {
        authenticate(true);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.png",
                "image/png",
                "png content".getBytes()
        );

        ResponseEntity<?> response = mediaController.uploadMedia(file, "secret-passwords");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error").toString()).contains("Invalid or disallowed folder destination");
    }

    @Test
    @DisplayName("uploadMedia returns 400 BAD REQUEST when file is empty")
    void uploadMediaEmptyFileReturnsBadRequest() {
        authenticate(true);
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
        authenticate(true);
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
