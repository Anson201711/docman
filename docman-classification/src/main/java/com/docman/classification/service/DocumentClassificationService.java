package com.docman.classification.service;

import com.docman.classification.entity.DocumentClassification;

import java.util.List;
import java.util.Map;

public interface DocumentClassificationService {

    DocumentClassification associate(String documentId, String classificationId);

    void disassociate(String documentId, String classificationId);

    List<String> getClassificationIdsByDocument(String documentId);

    List<String> getDocumentIdsByClassification(String classificationId);

    Map<String, Long> getStatistics();
}
