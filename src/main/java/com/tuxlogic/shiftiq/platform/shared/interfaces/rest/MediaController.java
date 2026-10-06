package com.tuxlogic.shiftiq.platform.shared.interfaces.rest;

import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/media", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Media", description = "Media and Image Upload Endpoints (Cloudinary)")
@PreAuthorize("isAuthenticated()")
public class MediaController {

    private final StorageService storageService;

    public MediaController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload an image or media file", description = "Uploads a multipart file to Cloudinary and returns its secure URL")
    public ResponseEntity<?> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "uploads") String folder) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File cannot be empty"));
        }
        try {
            String uploadedUrl = storageService.uploadFile(file, folder);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", uploadedUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to upload image to Cloudinary"));
        }
    }
}
