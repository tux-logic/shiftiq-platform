package com.tuxlogic.shiftiq.platform.shared.application.outboundservices.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file, String folderName);
    void deleteFile(String publicId);
}
