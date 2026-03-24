package com.docman.classification.controller;

import com.docman.classification.entity.Classification;
import com.docman.classification.service.ClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classification")
@RequiredArgsConstructor
public class ClassificationController {

    private final ClassificationService classificationService;

    @PostMapping
    public ResponseEntity<Classification> create(@RequestBody Classification classification) {
        return ResponseEntity.ok(classificationService.create(classification));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Classification> update(@PathVariable String id, @RequestBody Classification classification) {
        classification.setId(id);
        return ResponseEntity.ok(classificationService.update(classification));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        classificationService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Classification> getById(@PathVariable String id) {
        return ResponseEntity.ok(classificationService.getById(id));
    }

    @GetMapping("/tree")
    public ResponseEntity<List<Classification>> getTree() {
        return ResponseEntity.ok(classificationService.getTree());
    }

    @GetMapping("/children")
    public ResponseEntity<List<Classification>> getChildren(@RequestParam String parentId) {
        return ResponseEntity.ok(classificationService.getChildren(parentId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Classification>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(classificationService.search(keyword));
    }
}
