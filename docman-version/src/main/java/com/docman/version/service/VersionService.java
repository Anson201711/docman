package com.docman.version.service;

import com.docman.version.entity.DocumentVersion;

import java.util.List;

public interface VersionService {

    DocumentVersion create(DocumentVersion version);

    DocumentVersion getById(String id);

    List<DocumentVersion> getVersionsByDocument(String documentId);

    DocumentVersion getLatestVersion(String documentId);

    String compare(String versionId1, String versionId2);

    DocumentVersion rollback(String versionId);
}
