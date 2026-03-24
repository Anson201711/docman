package com.docman.version.controller;

import com.docman.version.entity.DocumentVersion;
import com.docman.version.service.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/version")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    @PostMapping
    public ResponseEntity<DocumentVersion> create(@RequestBody DocumentVersion version) {
        return ResponseEntity.ok(versionService.create(version));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentVersion> getById(@PathVariable String id) {
        return ResponseEntity.ok(versionService.getById(id));
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<DocumentVersion>> getVersionsByDocument(@PathVariable String documentId) {
        return ResponseEntity.ok(versionService.getVersionsByDocument(documentId));
    }

    @GetMapping("/document/{documentId}/latest")
    public ResponseEntity<DocumentVersion> getLatestVersion(@PathVariable String documentId) {
        return ResponseEntity.ok(versionService.getLatestVersion(documentId));
    }

    @GetMapping("/compare")
    public ResponseEntity<String> compare(
            @RequestParam String versionId1,
            @RequestParam String versionId2) {
        return ResponseEntity.ok(versionService.compare(versionId1, versionId2));
    }

    @PostMapping("/rollback/{versionId}")
    public ResponseEntity<DocumentVersion> rollback(@PathVariable String versionId) {
        return ResponseEntity.ok(versionService.rollback(versionId));
    }
}
