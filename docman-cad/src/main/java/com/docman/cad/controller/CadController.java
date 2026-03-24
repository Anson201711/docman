package com.docman.cad.controller;

import com.docman.cad.entity.CadFile;
import com.docman.cad.entity.Layer;
import com.docman.cad.service.CadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cad")
@RequiredArgsConstructor
public class CadController {

    private final CadService cadService;

    @GetMapping("/preview/{documentId}")
    public ResponseEntity<CadFile> preview(@PathVariable Long documentId) {
        return ResponseEntity.ok(cadService.preview(documentId));
    }

    @PostMapping("/thumbnail/{documentId}")
    public ResponseEntity<CadFile> generateThumbnail(@PathVariable Long documentId) {
        return ResponseEntity.ok(cadService.generateThumbnail(documentId));
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<CadFile> getCadFile(@PathVariable Long documentId) {
        return ResponseEntity.ok(cadService.getCadFile(documentId));
    }

    @PutMapping("/document")
    public ResponseEntity<CadFile> updateCadFile(@RequestBody CadFile cadFile) {
        return ResponseEntity.ok(cadService.updateCadFile(cadFile));
    }

    @GetMapping("/layers/{cadFileId}")
    public ResponseEntity<List<Layer>> getLayers(@PathVariable Long cadFileId) {
        return ResponseEntity.ok(cadService.getLayers(cadFileId));
    }

    @GetMapping("/layer/{layerId}")
    public ResponseEntity<Layer> getLayer(@PathVariable Long layerId) {
        return ResponseEntity.ok(cadService.getLayer(layerId));
    }

    @PutMapping("/layer")
    public ResponseEntity<Layer> updateLayer(@RequestBody Layer layer) {
        return ResponseEntity.ok(cadService.updateLayer(layer));
    }

    @PutMapping("/layer/{layerId}/visibility")
    public ResponseEntity<Void> toggleLayerVisibility(@PathVariable Long layerId, @RequestParam Boolean visible) {
        cadService.toggleLayerVisibility(layerId, visible);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/layer/{layerId}/lock")
    public ResponseEntity<Void> toggleLayerLock(@PathVariable Long layerId, @RequestParam Boolean locked) {
        cadService.toggleLayerLock(layerId, locked);
        return ResponseEntity.ok().build();
    }
}
