// src/main/java/com/penglobe/server/service/UploadService.java
package com.penglobe.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    @Value("${app.upload.dir}")
    private String baseDir; // 예: /var/www/uploads or C:/uploads

    @Value("${app.upload.base-path:/uploads}")
    private String basePath; // 예: /uploads

    private static final Set<String> ALLOWED = Set.of(
            "image/jpeg","image/jpg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/svg+xml",
            "image/heic","image/heif"
    );

    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 없습니다.");
        }
        if (baseDir == null || baseDir.isBlank()) {
            throw new IllegalStateException("app.upload.dir이 설정되지 않았습니다.");
        }

        final String ct = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase();
        if (!ALLOWED.contains(ct)) {
            throw new IllegalArgumentException("허용되지 않은 이미지 형식: " + ct);
        }

        final String ext = switch (ct) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "image/svg+xml" -> ".svg";
            case "image/heic", "image/heif" -> ".heic";
            default -> "";
        };

        LocalDate now = LocalDate.now();
        Path dir = Paths.get(baseDir, String.valueOf(now.getYear()), String.format("%02d", now.getMonthValue()));
        Files.createDirectories(dir); // 폴더 없으면 생성 (권한 없으면 여기서 예외)

        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dest = dir.resolve(filename);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        // DB에 저장할 "공개 경로" (/uploads/2025/09/xxx.ext)
        return basePath + "/" + now.getYear() + "/" + String.format("%02d", now.getMonthValue()) + "/" + filename;
    }

    public void deleteIfExists(String relPath) {
        if (relPath == null || relPath.isBlank()) return;
        if (baseDir == null || baseDir.isBlank()) return;
        if (basePath == null || basePath.isBlank()) return;

        // /uploads/.. → 로컬 절대 경로로 변환
        String sub = relPath.startsWith(basePath) ? relPath.substring(basePath.length()) : relPath;
        if (sub.startsWith("/")) sub = sub.substring(1);

        Path p = Paths.get(baseDir, sub.replace("/", File.separator));
        try {
            Files.deleteIfExists(p);
        } catch (IOException ignore) { /* 파일 없으면 무시 */ }
    }
}
