package com.giang.rentalEstate.service.impl;
import com.giang.rentalEstate.config.GoogleDriveConfig;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleDriveService {
    private final GoogleDriveConfig googleDriveConfig;
    private static final String FOLDER_ID = "1ssP3GlWBIF3vqeNiOsmFgdATy2C6NVTN"; // ID thư mục của bạn
    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveService.class);

    public String uploadFile(MultipartFile file) throws IOException, GeneralSecurityException {
        logger.info("Bắt đầu upload file: {}", file.getOriginalFilename());
        Drive driveService = googleDriveConfig.getDriveService();

        File fileMetadata = new File();
        fileMetadata.setName(generateUniqueFileName(file.getOriginalFilename()));
        fileMetadata.setParents(Collections.singletonList(FOLDER_ID));

        // Set permissions để file có thể xem công khai
        Permission permission = new Permission()
                .setType("anyone")
                .setRole("reader");

        //Upload file
        InputStream inputStream = file.getInputStream();
        AbstractInputStreamContent content = new com.google.api.client.http.InputStreamContent(
                file.getContentType(), inputStream);

        File uploadedFile = driveService.files().create(fileMetadata, content)
                .setFields("id, webContentLink, webViewLink")
                .execute();
        driveService.permissions().create(uploadedFile.getId(), permission)
                .execute();
        logger.info("Upload thành công file: {}", uploadedFile.getId());
        return uploadedFile.getWebViewLink();
    }

    private String generateUniqueFileName(String originalFilename) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String random = UUID.randomUUID().toString().substring(0, 8);
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return String.format("%s_%s%s", timestamp, random, extension);
    }

    public void deleteFile(String fileId) throws IOException {
        try {
            logger.info("Deleting file with ID: {}", fileId);
            Drive driveService = googleDriveConfig.getDriveService();
            driveService.files().delete(fileId).execute();
            logger.info("File deleted successfully from Google Drive");
        } catch (Exception e) {
            logger.error("Error deleting file from Google Drive: {}", e.getMessage());
            throw new IOException("Failed to delete file from Google Drive: " + e.getMessage());
        }
    }
}


