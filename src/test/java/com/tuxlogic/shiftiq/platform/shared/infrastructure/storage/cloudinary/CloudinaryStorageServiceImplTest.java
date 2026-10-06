package com.tuxlogic.shiftiq.platform.shared.infrastructure.storage.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CloudinaryStorageServiceImplTest {

    private Cloudinary cloudinary;
    private Uploader uploader;
    private CloudinaryStorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        cloudinary = Mockito.mock(Cloudinary.class);
        uploader = Mockito.mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        storageService = new CloudinaryStorageServiceImpl(cloudinary);
    }

    @Test
    @DisplayName("uploadFile succeeds when a valid image file is provided")
    void uploadFileSuccess() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/demo/image/upload/v1/shiftiq/customers/avatar.jpg"));

        String resultUrl = storageService.uploadFile(file, "customers");

        assertThat(resultUrl).isEqualTo("https://res.cloudinary.com/demo/image/upload/v1/shiftiq/customers/avatar.jpg");
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    @DisplayName("uploadFile throws IllegalArgumentException when file is empty")
    void uploadFileThrowsExceptionWhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        assertThatThrownBy(() -> storageService.uploadFile(emptyFile, "customers"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("shared.error.storage.fileEmpty");
    }

    @Test
    @DisplayName("uploadFile throws IllegalArgumentException when file type is not allowed (e.g. .exe)")
    void uploadFileThrowsExceptionWhenMimeTypeNotAllowed() {
        MockMultipartFile exeFile = new MockMultipartFile(
                "file",
                "malware.exe",
                "application/x-msdownload",
                "binary data".getBytes()
        );

        assertThatThrownBy(() -> storageService.uploadFile(exeFile, "uploads"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only image files");
    }

    @Test
    @DisplayName("deleteFile calls Cloudinary uploader destroy")
    void deleteFileSuccess() throws IOException {
        when(uploader.destroy(any(), anyMap())).thenReturn(Map.of("result", "ok"));

        storageService.deleteFile("shiftiq/uploads/sample_123");

        verify(uploader).destroy(Mockito.eq("shiftiq/uploads/sample_123"), anyMap());
    }
}
