package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-file-size:10485760}") // 10MB default
    private long maxFileSize;

    private final List<String> allowedMimeTypes = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif",
        "application/pdf"
    );

    private final List<String> allowedExtensions = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".pdf"
    );

    public String uploadPrescriptionFile(MultipartFile file, Long prescriptionId) throws IOException {
        validateFile(file);
        
        // Create directory structure: uploads/prescriptions/YYYY/MM/
        LocalDate now = LocalDate.now();
        String yearMonth = now.getYear() + "/" + String.format("%02d", now.getMonthValue());
        Path uploadPath = Paths.get(uploadDir, "prescriptions", yearMonth);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = "prescription_" + prescriptionId + "_" + UUID.randomUUID().toString() + extension;
        
        Path filePath = uploadPath.resolve(uniqueFilename);
        
        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path that can be used as URL
        return "/uploads/prescriptions/" + yearMonth + "/" + uniqueFilename;
    }

    public String uploadDeliveryProof(MultipartFile file, String trackingNumber) throws IOException {
        validateFile(file);
        
        // Create directory structure: uploads/delivery-proofs/YYYY/MM/
        LocalDate now = LocalDate.now();
        String yearMonth = now.getYear() + "/" + String.format("%02d", now.getMonthValue());
        Path uploadPath = Paths.get(uploadDir, "delivery-proofs", yearMonth);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = "delivery_" + trackingNumber + "_" + UUID.randomUUID().toString() + extension;
        
        Path filePath = uploadPath.resolve(uniqueFilename);
        
        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path that can be used as URL
        return "/uploads/delivery-proofs/" + yearMonth + "/" + uniqueFilename;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }

        String contentType = file.getContentType();
        if (!allowedMimeTypes.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: " + allowedMimeTypes);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename is required");
        }

        String extension = getFileExtension(filename).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("File extension not allowed. Allowed extensions: " + allowedExtensions);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        
        return filename.substring(lastDotIndex);
    }

    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadDir).resolve(filePath.substring(1)); // Remove leading slash
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    public boolean fileExists(String filePath) {
        try {
            Path path = Paths.get(uploadDir).resolve(filePath.substring(1)); // Remove leading slash
            return Files.exists(path);
        } catch (Exception e) {
            return false;
        }
    }
}
