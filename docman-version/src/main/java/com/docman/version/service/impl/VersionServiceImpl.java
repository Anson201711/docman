package com.docman.version.service.impl;

import com.docman.version.entity.DocumentVersion;
import com.docman.version.service.VersionService;
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
public class VersionServiceImpl implements VersionService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String VERSION_KEY = "version:doc:";
    private static final String LATEST_KEY = "version:latest:";

    @Override
    public DocumentVersion create(DocumentVersion version) {
        version.setId(UUID.randomUUID().toString());
        version.setCreatedAt(LocalDateTime.now());
        version.setStatus("ACTIVE");

        Integer latestVersion = getNextVersionNumber(version.getDocumentId());
        version.setVersionNumber(latestVersion);

        String key = VERSION_KEY + version.getDocumentId();
        redisTemplate.opsForList().rightPush(key, version);
        redisTemplate.opsForValue().set(LATEST_KEY + version.getDocumentId(), version);

        log.info("Created version {} for document {}", version.getVersionNumber(), version.getDocumentId());
        return version;
    }

    @Override
    public DocumentVersion getById(String id) {
        return null;
    }

    @Override
    public List<DocumentVersion> getVersionsByDocument(String documentId) {
        String key = VERSION_KEY + documentId;
        List<Object> versions = redisTemplate.opsForList().range(key, 0, -1);
        if (versions != null) {
            return versions.stream()
                    .map(obj -> (DocumentVersion) obj)
                    .sorted(Comparator.comparing(DocumentVersion::getVersionNumber).reversed())
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public DocumentVersion getLatestVersion(String documentId) {
        Object cached = redisTemplate.opsForValue().get(LATEST_KEY + documentId);
        if (cached != null) {
            return (DocumentVersion) cached;
        }

        List<DocumentVersion> versions = getVersionsByDocument(documentId);
        if (!versions.isEmpty()) {
            return versions.get(0);
        }
        return null;
    }

    @Override
    public String compare(String versionId1, String versionId2) {
        return "Version comparison not implemented yet";
    }

    @Override
    public DocumentVersion rollback(String versionId) {
        return null;
    }

    private Integer getNextVersionNumber(String documentId) {
        List<DocumentVersion> versions = getVersionsByDocument(documentId);
        if (versions.isEmpty()) {
            return 1;
        }
        return versions.get(0).getVersionNumber() + 1;
    }
}
