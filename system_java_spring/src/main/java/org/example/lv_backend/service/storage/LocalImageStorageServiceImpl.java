package org.example.lv_backend.service.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;

@Service
public class LocalImageStorageServiceImpl implements ImageStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.base.url}")
    private String baseUrl;

    @Override
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            return null;

        validateImage(file);

        try {
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            String originalFilename = file.getOriginalFilename();
            String extension = StringUtils.getFilenameExtension(originalFilename);
            
            String newFilename = UUID.randomUUID().toString(); 
            if (extension != null) {
                newFilename = newFilename + "." + extension;
            }
            
            file.transferTo(uploadPath.resolve(newFilename));
            return baseUrl + "/" + uploadDir + newFilename;

        } catch (Exception e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty())
            return;

        if (!fileUrl.contains("/" + uploadDir)) {
            return;
        }

        try {
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            
            Path filePath = Paths.get(uploadDir).resolve(filename);
            
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            throw new AppException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException(ErrorCode.INVALID_IMAGE_FILE);
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new AppException(ErrorCode.INVALID_IMAGE_FILE);
        }
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new AppException(ErrorCode.INVALID_IMAGE_FILE);
        }
        String ext = originalFilename.substring(lastDotIndex + 1).toLowerCase();
        if (!ext.equals("png") && !ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("webp")) {
            throw new AppException(ErrorCode.INVALID_IMAGE_FILE);
        }
    }
}
