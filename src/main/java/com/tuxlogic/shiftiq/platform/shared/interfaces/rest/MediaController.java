package com.tuxlogic.shiftiq.platform.shared.interfaces.rest;

import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Media", description = "Media and Image Upload Endpoints (Cloudinary Integration)")
@PreAuthorize("isAuthenticated()")
public class MediaController {

    private final StorageService storageService;

    public MediaController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload an image file",
            description = "Uploads an image file (JPEG, PNG, WEBP, GIF, SVG, AVIF; max 10MB) to Cloudinary and returns its secure URL."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Image uploaded successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "{\"url\": \"https://res.cloudinary.com/demo/image/upload/v1234/shiftiq/uploads/sample.jpg\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid file or unsupported format",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "{\"error\": \"Only image files (JPEG, PNG, WEBP, GIF, SVG, AVIF) are allowed.\"}"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Bearer JWT token missing or invalid"),
            @ApiResponse(responseCode = "413", description = "File size exceeds 10MB limit"),
            @ApiResponse(responseCode = "500", description = "Cloudinary storage service error")
    })
    public ResponseEntity<?> uploadMedia(
            @Parameter(description = "Image file to upload (JPEG, PNG, WEBP, GIF, SVG, AVIF max 10MB)", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Destination folder inside Cloudinary (e.g., 'customers', 'employees', 'products', 'vehicles', 'workorders')", required = false)
            @RequestParam(value = "folder", required = false, defaultValue = "uploads") String folder) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File cannot be empty"));
        }
        try {
            String uploadedUrl = storageService.uploadFile(file, folder);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", uploadedUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to upload image to Cloudinary: " + e.getMessage()));
        }
    }
}

