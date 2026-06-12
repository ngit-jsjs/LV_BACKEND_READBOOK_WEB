package org.example.lv_backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    
    String storeFile(MultipartFile file);

    void deleteFile(String fileUrl);
}
