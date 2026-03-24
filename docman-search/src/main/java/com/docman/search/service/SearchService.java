package com.docman.search.service;

import com.docman.search.entity.DocumentIndex;

import java.util.List;
import java.util.Map;

public interface SearchService {

    void indexDocument(DocumentIndex document);

    void updateIndex(DocumentIndex document);

    void deleteIndex(String documentId);

    List<DocumentIndex> search(String keyword, int page, int size);

    List<String> suggest(String prefix);

    Map<String, Long> getStatistics();
}
