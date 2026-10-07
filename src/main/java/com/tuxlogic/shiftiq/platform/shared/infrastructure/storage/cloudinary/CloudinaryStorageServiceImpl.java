package com.tuxlogic.shiftiq.platform.shared.infrastructure.storage.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class CloudinaryStorageServiceImpl implements StorageService {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/svg+xml",
            "image/avif"
    );

    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryStorageServiceImpl(
            @Value("${cloudinary.cloud-name:${CLOUDINARY_CLOUD_NAME:}}") String cloudName,
            @Value("${cloudinary.api-key:${CLOUDINARY_API_KEY:}}") String apiKey,
            @Value("${cloudinary.api-secret:${CLOUDINARY_API_SECRET:}}") String apiSecret) {

        if (cloudName == null || cloudName.isBlank() ||
            apiKey == null || apiKey.isBlank() ||
            apiSecret == null || apiSecret.isBlank()) {
            log.warn("Cloudinary credentials are not configured. Upload operations will fail until valid credentials are provided.");
        }

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public CloudinaryStorageServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadFile(MultipartFile file, String folderName) {
        validateFile(file);

        try {
            String sanitizedFolder = sanitizeFolder(folderName);
            String originalFilename = file.getOriginalFilename();
            String publicId = generateUniquePublicId(originalFilename);

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", sanitizedFolder,
                    "public_id", publicId,
                    "unique_filename", true,
                    "overwrite", false,
                    "resource_type", "image"
            ));

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("File '{}' uploaded successfully to Cloudinary folder '{}': {}", originalFilename, sanitizedFolder, secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary: {}", e.getMessage(), e);
            throw new IllegalStateException("shared.error.storage.uploadFailed", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("shared.error.storage.fileEmpty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size exceeds maximum allowed limit of 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only image files (JPEG, PNG, WEBP, GIF, SVG, AVIF) are allowed. Provided type: " + contentType);
        }
    }

    private String sanitizeFolder(String folderName) {
        if (folderName == null || folderName.isBlank()) {
            return "shiftiq/uploads";
        }
        String cleanFolder = folderName.replaceAll("[^a-zA-Z0-9/_.-]", "");
        if (cleanFolder.isBlank()) {
            return "shiftiq/uploads";
        }
        return cleanFolder.startsWith("shiftiq/") ? cleanFolder : "shiftiq/" + cleanFolder;
    }

    private String generateUniquePublicId(String originalFilename) {
        String baseName = "file";
        if (originalFilename != null && !originalFilename.isBlank()) {
            int lastDot = originalFilename.lastIndexOf('.');
            String nameWithoutExt = (lastDot > 0) ? originalFilename.substring(0, lastDot) : originalFilename;
            baseName = nameWithoutExt.replaceAll("[^a-zA-Z0-9_-]", "_");
        }
        return baseName + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public void deleteFile(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("File with public ID '{}' deleted from Cloudinary", publicId);
        } catch (IOException e) {
            log.error("Failed to delete file from Cloudinary with public ID '{}': {}", publicId, e.getMessage(), e);
        }
    }
}

