package com.tuxlogic.shiftiq.platform.shared.interfaces.rest;

import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.storage.StorageService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.AuthenticatedPrincipal;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/v1/media", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Media", description = "Media and Image Upload Endpoints (Cloudinary Integration)")
@PreAuthorize("isAuthenticated()")
public class MediaController {

    private static final Set<String> ALLOWED_FOLDERS = Set.of(
            "customers", "employees", "products", "vehicles", "workorders", "uploads"
    );

    private static final Set<String> OPERATIONAL_FOLDERS = Set.of(
            "employees", "products", "vehicles", "workorders"
    );

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
            @ApiResponse(responseCode = "403", description = "Forbidden - Caller not authorized to upload to target folder"),
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

        String normalizedFolder = (folder == null || folder.isBlank()) ? "uploads" : folder.toLowerCase(Locale.ROOT).trim();
        if (!ALLOWED_FOLDERS.contains(normalizedFolder)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid or disallowed folder destination: '" + folder + "'. Allowed folders: " + ALLOWED_FOLDERS
            ));
        }

        if (OPERATIONAL_FOLDERS.contains(normalizedFolder)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (!isStaffUser(auth)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "error", "Unauthorized: Only workshop staff members are allowed to upload to folder '" + normalizedFolder + "'."
                ));
            }
        }

        try {
            String uploadedUrl = storageService.uploadFile(file, normalizedFolder);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", uploadedUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to upload image to Cloudinary: " + e.getMessage()));
        }
    }

    private boolean isStaffUser(Authentication auth) {
        if (auth == null) {
            return false;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof AuthenticatedPrincipal authenticatedPrincipal) {
            return authenticatedPrincipal.hasPermission(Permission.MEDIA_UPLOAD.name())
                    || authenticatedPrincipal.hasRole("ROLE_ADMIN")
                    || authenticatedPrincipal.hasRole("ROLE_OWNER")
                    || authenticatedPrincipal.hasRole("ROLE_BRANCH_MANAGER")
                    || authenticatedPrincipal.hasRole("ROLE_EMPLOYEE")
                    || authenticatedPrincipal.hasRole("ROLE_ASSISTANT");
        }
        return auth.getAuthorities().stream().anyMatch(a -> {
            String role = a.getAuthority();
            return role.equals(Permission.MEDIA_UPLOAD.name())
                    || role.equals("ROLE_ADMIN") || role.equals("ROLE_OWNER")
                    || role.equals("ROLE_BRANCH_MANAGER") || role.equals("ROLE_EMPLOYEE")
                    || role.equals("ROLE_ASSISTANT");
        });
    }
}
