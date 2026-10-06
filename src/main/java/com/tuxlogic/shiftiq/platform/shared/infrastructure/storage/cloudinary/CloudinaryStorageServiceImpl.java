package com.tuxlogic.shiftiq.platform.shared.infrastructure.storage.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryStorageServiceImpl implements StorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageServiceImpl(
            @Value("${cloudinary.cloud-name:${CLOUDINARY_CLOUD_NAME:z9rmazyn}}") String cloudName,
            @Value("${cloudinary.api-key:${CLOUDINARY_API_KEY:942975311966939}}") String apiKey,
            @Value("${cloudinary.api-secret:${CLOUDINARY_API_SECRET:dHSkRRwYxmdolUub1HN2u3mJBYM}}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    @Override
    public String uploadFile(MultipartFile file, String folderName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("shared.error.storage.fileEmpty");
        }
        try {
            String folder = folderName != null && !folderName.isBlank() ? "shiftiq/" + folderName : "shiftiq/uploads";
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto"
            ));
            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("File uploaded successfully to Cloudinary folder '{}': {}", folder, secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary: {}", e.getMessage(), e);
            throw new IllegalStateException("shared.error.storage.uploadFailed", e);
        }
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
