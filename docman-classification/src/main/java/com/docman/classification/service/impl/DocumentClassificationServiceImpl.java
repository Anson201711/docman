package com.docman.classification.service.impl;

import com.docman.classification.entity.DocumentClassification;
import com.docman.classification.service.DocumentClassificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentClassificationServiceImpl implements DocumentClassificationService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DOC_CLASS_KEY = "doc:classification:";
    private static final String CLASS_DOC_KEY = "classification:doc:";
    private static final String STATS_KEY = "classification:stats";

    @Override
    public DocumentClassification associate(String documentId, String classificationId) {
        DocumentClassification dc = new DocumentClassification();
        dc.setId(UUID.randomUUID().toString());
        dc.setDocumentId(documentId);
        dc.setClassificationId(classificationId);
        dc.setCreatedAt(LocalDateTime.now());
        dc.setStatus("ACTIVE");

        String docKey = DOC_CLASS_KEY + documentId;
        String classKey = CLASS_DOC_KEY + classificationId;

        redisTemplate.opsForSet().add(docKey, classificationId);
        redisTemplate.opsForSet().add(classKey, documentId);
        redisTemplate.delete(STATS_KEY);

        log.info("Associated document {} with classification {}", documentId, classificationId);
        return dc;
    }

    @Override
    public void disassociate(String documentId, String classificationId) {
        String docKey = DOC_CLASS_KEY + documentId;
        String classKey = CLASS_DOC_KEY + classificationId;

        redisTemplate.opsForSet().remove(docKey, classificationId);
        redisTemplate.opsForSet().remove(classKey, documentId);
        redisTemplate.delete(STATS_KEY);

        log.info("Disassociated document {} from classification {}", documentId, classificationId);
    }

    @Override
    public List<String> getClassificationIdsByDocument(String documentId) {
        String docKey = DOC_CLASS_KEY + documentId;
        Set<Object> members = redisTemplate.opsForSet().members(docKey);
        if (members != null) {
            return members.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getDocumentIdsByClassification(String classificationId) {
        String classKey = CLASS_DOC_KEY + classificationId;
        Set<Object> members = redisTemplate.opsForSet().members(classKey);
        if (members != null) {
            return members.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, Long> getStatistics() {
        Object cached = redisTemplate.opsForValue().get(STATS_KEY);
        if (cached != null) {
            return (Map<String, Long>) cached;
        }

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalAssociations", 0L);
        stats.put("totalClassifications", 0L);

        redisTemplate.opsForValue().set(STATS_KEY, stats);
        return stats;
    }
}
