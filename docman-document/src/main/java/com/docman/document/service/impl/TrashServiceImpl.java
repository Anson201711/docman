package com.docman.document.service.impl;

import com.docman.document.entity.Document;
import com.docman.document.entity.TrashRecord;
import com.docman.document.repository.DocumentRepository;
import com.docman.document.repository.FolderRepository;
import com.docman.document.repository.TrashRecordRepository;
import com.docman.document.service.TrashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrashServiceImpl implements TrashService {

    private final TrashRecordRepository trashRecordRepository;
    private final DocumentRepository documentRepository;
    private final FolderRepository folderRepository;

    private static final int RETENTION_DAYS = 30;

    @Override
    public List<TrashRecord> getTrashList(String ownerId) {
        return trashRecordRepository.findByOwnerId(ownerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveToTrash(Long documentId, String ownerId) {
        Document document = documentRepository.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("Document not found: " + documentId);
        }

        TrashRecord existingRecord = trashRecordRepository.findByDocumentId(documentId);
        if (existingRecord != null) {
            throw new RuntimeException("Document already in trash: " + documentId);
        }

        TrashRecord trashRecord = new TrashRecord();
        trashRecord.setDocumentId(documentId);
        trashRecord.setDocumentName(document.getName());
        trashRecord.setFolderId(document.getFolderId());
        trashRecord.setFolderPath(document.getPath());
        trashRecord.setOwnerId(ownerId);
        trashRecord.setRecordType(1);
        trashRecord.setOriginalStatus(document.getStatus());
        trashRecord.setDeleteTime(LocalDateTime.now());
        trashRecord.setExpireTime(LocalDateTime.now().plusDays(RETENTION_DAYS));

        trashRecordRepository.insert(trashRecord);

        documentRepository.deleteById(documentId);
        log.info("Moved document {} to trash, will expire at {}", documentId, trashRecord.getExpireTime());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrashRecord restoreFromTrash(Long documentId) {
        TrashRecord trashRecord = trashRecordRepository.findByDocumentId(documentId);
        if (trashRecord == null) {
            throw new RuntimeException("Trash record not found for document: " + documentId);
        }

        Document document = new Document();
        document.setId(trashRecord.getDocumentId());
        document.setName(trashRecord.getDocumentName());
        document.setFolderId(trashRecord.getFolderId());
        document.setPath(trashRecord.getFolderPath());
        document.setOwnerId(trashRecord.getOwnerId());
        document.setStatus(trashRecord.getOriginalStatus());

        documentRepository.insert(document);

        trashRecordRepository.deleteById(trashRecord.getId());
        log.info("Restored document {} from trash", documentId);

        return trashRecord;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void permanentDelete(Long documentId) {
        TrashRecord trashRecord = trashRecordRepository.findByDocumentId(documentId);
        if (trashRecord != null) {
            trashRecordRepository.deleteById(trashRecord.getId());
        }

        trashRecordRepository.deleteByDocumentId(documentId);
        log.info("Permanently deleted document {}", documentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void emptyTrash(String ownerId) {
        List<TrashRecord> records = trashRecordRepository.findByOwnerId(ownerId);
        for (TrashRecord record : records) {
            permanentDelete(record.getDocumentId());
        }
        log.info("Emptied trash for owner: {}", ownerId);
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredRecords() {
        List<TrashRecord> expiredRecords = trashRecordRepository.findExpiredRecords();
        for (TrashRecord record : expiredRecords) {
            permanentDelete(record.getDocumentId());
            log.info("Cleaned expired trash record for document: {}", record.getDocumentId());
        }
    }
}
