package com.penglobe.server.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class DownloadController {

    @Value("${app.download-path:/app/download/penglobe.apk}")
    private String apkPath;

    @GetMapping("/download/penglobe.apk")
    public ResponseEntity<Resource> downloadApk() {
        File file = new File(apkPath);

        if (!file.exists()) {
            // 404 응답 (JSON or text)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=penglobe.apk")
                .contentType(MediaType.parseMediaType("application/vnd.android.package-archive"))
                .body(resource);
    }

}
