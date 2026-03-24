package com.docman.classification.controller;

import com.docman.classification.entity.DocumentClassification;
import com.docman.classification.service.DocumentClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/document-classification")
@RequiredArgsConstructor
public class DocumentClassificationController {

    private final DocumentClassificationService documentClassificationService;

    @PostMapping("/associate")
    public ResponseEntity<DocumentClassification> associate(
            @RequestParam String documentId,
            @RequestParam String classificationId) {
        return ResponseEntity.ok(documentClassificationService.associate(documentId, classificationId));
    }

    @DeleteMapping("/disassociate")
    public ResponseEntity<Void> disassociate(
            @RequestParam String documentId,
            @RequestParam String classificationId) {
        documentClassificationService.disassociate(documentId, classificationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<String>> getClassificationIdsByDocument(@PathVariable String documentId) {
        return ResponseEntity.ok(documentClassificationService.getClassificationIdsByDocument(documentId));
    }

    @GetMapping("/classification/{classificationId}")
    public ResponseEntity<List<String>> getDocumentIdsByClassification(@PathVariable String classificationId) {
        return ResponseEntity.ok(documentClassificationService.getDocumentIdsByClassification(classificationId));
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        return ResponseEntity.ok(documentClassificationService.getStatistics());
    }
}
