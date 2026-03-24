package com.docman.cad.service.impl;

import com.docman.cad.entity.CadFile;
import com.docman.cad.entity.Layer;
import com.docman.cad.service.CadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CadServiceImpl implements CadService {

    private final Map<Long, CadFile> cadFileStore = new ConcurrentHashMap<>();
    private final Map<Long, Layer> layerStore = new ConcurrentHashMap<>();
    private final AtomicLong cadFileIdCounter = new AtomicLong(1);
    private final AtomicLong layerIdCounter = new AtomicLong(1);

    @Override
    public CadFile preview(Long documentId) {
        log.info("Preview CAD file for document {}", documentId);
        CadFile cadFile = cadFileStore.values().stream()
                .filter(cf -> documentId.equals(cf.getDocumentId()))
                .findFirst()
                .orElse(null);
        if (cadFile == null) {
            cadFile = new CadFile();
            cadFile.setId(cadFileIdCounter.getAndIncrement());
            cadFile.setDocumentId(documentId);
            cadFile.setStatus("PREVIEW_READY");
            cadFile.setCreateTime(LocalDateTime.now());
            cadFileStore.put(cadFile.getId(), cadFile);
        }
        return cadFile;
    }

    @Override
    public CadFile generateThumbnail(Long documentId) {
        log.info("Generate thumbnail for CAD document {}", documentId);
        CadFile cadFile = preview(documentId);
        cadFile.setThumbnailPath("/thumbnails/" + documentId + ".png");
        cadFile.setUpdateTime(LocalDateTime.now());
        return cadFile;
    }

    @Override
    public CadFile getCadFile(Long documentId) {
        return cadFileStore.values().stream()
                .filter(cf -> documentId.equals(cf.getDocumentId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public CadFile updateCadFile(CadFile cadFile) {
        cadFile.setUpdateTime(LocalDateTime.now());
        cadFileStore.put(cadFile.getId(), cadFile);
        return cadFile;
    }

    @Override
    public List<Layer> getLayers(Long cadFileId) {
        return layerStore.values().stream()
                .filter(l -> cadFileId.equals(l.getCadFileId()))
                .collect(Collectors.toList());
    }

    @Override
    public Layer getLayer(Long layerId) {
        return layerStore.get(layerId);
    }

    @Override
    public Layer updateLayer(Layer layer) {
        layerStore.put(layer.getId(), layer);
        return layer;
    }

    @Override
    public void toggleLayerVisibility(Long layerId, Boolean visible) {
        Layer layer = layerStore.get(layerId);
        if (layer != null) {
            layer.setVisible(visible);
            layerStore.put(layerId, layer);
            log.info("Layer {} visibility set to {}", layerId, visible);
        }
    }

    @Override
    public void toggleLayerLock(Long layerId, Boolean locked) {
        Layer layer = layerStore.get(layerId);
        if (layer != null) {
            layer.setLocked(locked);
            layerStore.put(layerId, layer);
            log.info("Layer {} lock set to {}", layerId, locked);
        }
    }
}
