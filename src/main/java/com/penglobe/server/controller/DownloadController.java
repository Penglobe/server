package com.penglobe.server.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class DownloadController {

    // application.yml 에서 경로 주입받음 (환경마다 다르게 설정 가능)
    @Value("${app.download-path:/app/download/penglobe.apk}")
    private String apkPath;

    @GetMapping(value = "/download/penglobe.apk", produces = "application/vnd.android.package-archive")
    public ResponseEntity<Resource> downloadApk() {
        File file = new File(apkPath);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=penglobe.apk")
                .contentType(MediaType.parseMediaType("application/vnd.android.package-archive"))
                .body(resource);
    }
}
