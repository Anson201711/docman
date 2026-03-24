package com.docman.document.service;

import com.docman.document.entity.TrashRecord;

import java.util.List;

public interface TrashService {

    List<TrashRecord> getTrashList(String ownerId);

    void moveToTrash(Long documentId, String ownerId);

    TrashRecord restoreFromTrash(Long documentId);

    void permanentDelete(Long documentId);

    void emptyTrash(String ownerId);

    void cleanExpiredRecords();
}
