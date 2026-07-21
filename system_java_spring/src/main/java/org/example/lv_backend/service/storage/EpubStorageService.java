package org.example.lv_backend.service.storage;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class EpubStorageService {

    @Value("${app.upload.epub-dir}")
    private String epubDir;
    private void validateEpubFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.EPUB_FILE_EMPTY);
        }
//error code
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".epub")) {
            throw new AppException(ErrorCode.EPUB_FILE_INVALID_FORMAT);
        }
        //error code
    }
    public String storeFile(MultipartFile file) {
        validateEpubFile(file);
        Path storageDir = Paths.get(epubDir);
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
        String storedFilename = UUID.randomUUID() + ".epub";
        Path storedPath = storageDir.resolve(storedFilename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, storedPath, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
        return storedPath.toAbsolutePath().toString();
    }
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty())
            return;

        try {
            Path filePath = Paths.get(fileUrl);
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            throw new AppException(ErrorCode.FILE_DELETE_FAILED);
        }
    }
}
