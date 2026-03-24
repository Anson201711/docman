package com.docman.cad.service;

import com.docman.cad.entity.CadFile;
import com.docman.cad.entity.Layer;

import java.util.List;

public interface CadService {
    CadFile preview(Long documentId);
    CadFile generateThumbnail(Long documentId);
    CadFile getCadFile(Long documentId);
    CadFile updateCadFile(CadFile cadFile);

    List<Layer> getLayers(Long cadFileId);
    Layer getLayer(Long layerId);
    Layer updateLayer(Layer layer);
    void toggleLayerVisibility(Long layerId, Boolean visible);
    void toggleLayerLock(Long layerId, Boolean locked);
}
