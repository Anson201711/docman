package com.docman.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.docman.search.entity.DocumentIndex;
import com.docman.search.service.SearchService;
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
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String INDEX_NAME = "documents";
    private static final String SEARCH_CACHE_KEY = "search:cache:";
    private static final String SUGGEST_KEY = "search:suggest:";

    @Override
    public void indexDocument(DocumentIndex document) {
        try {
            ensureIndexExists();

            if (document.getIndexedAt() == null) {
                document.setIndexedAt(LocalDateTime.now());
            }
            if (document.getStatus() == null) {
                document.setStatus("ACTIVE");
            }

            IndexRequest<DocumentIndex> request = IndexRequest.of(i -> i
                    .index(INDEX_NAME)
                    .id(document.getDocumentId())
                    .document(document)
            );

            elasticsearchClient.index(request);

            String suggestKey = SUGGEST_KEY + document.getDocumentId();
            redisTemplate.opsForList().rightPush(suggestKey, document.getTitle());

            log.info("Indexed document: {}", document.getDocumentId());
        } catch (Exception e) {
            log.error("Failed to index document: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to index document: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateIndex(DocumentIndex document) {
        try {
            document.setIndexedAt(LocalDateTime.now());

            UpdateRequest<DocumentIndex, DocumentIndex> request = UpdateRequest.of(u -> u
                    .index(INDEX_NAME)
                    .id(document.getDocumentId())
                    .doc(document)
            );

            elasticsearchClient.update(request, DocumentIndex.class);

            log.info("Updated index for document: {}", document.getDocumentId());
        } catch (Exception e) {
            log.error("Failed to update index: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update index: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteIndex(String documentId) {
        try {
            DocumentIndex document = new DocumentIndex();
            document.setDocumentId(documentId);
            document.setStatus("DELETED");

            UpdateRequest<DocumentIndex, DocumentIndex> request = UpdateRequest.of(u -> u
                    .index(INDEX_NAME)
                    .id(documentId)
                    .doc(document)
            );

            elasticsearchClient.update(request, DocumentIndex.class);

            String suggestKey = SUGGEST_KEY + documentId;
            redisTemplate.delete(suggestKey);

            log.info("Deleted index for document: {}", documentId);
        } catch (Exception e) {
            log.error("Failed to delete index: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete index: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DocumentIndex> search(String keyword, int page, int size) {
        try {
            ensureIndexExists();

            String cacheKey = SEARCH_CACHE_KEY + keyword + ":" + page + ":" + size;

            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return (List<DocumentIndex>) cached;
            }

            Query multiMatchQuery = MultiMatchQuery.of(m -> m
                    .query(keyword)
                    .fields("content", "title", "keywords")
            )._toQuery();

            SearchRequest request = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .query(multiMatchQuery)
                    .from(page * size)
                    .size(size)
            );

            SearchResponse<DocumentIndex> response = elasticsearchClient.search(request, DocumentIndex.class);

            List<DocumentIndex> results = response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            redisTemplate.opsForValue().set(cacheKey, results);

            return results;
        } catch (Exception e) {
            log.error("Search failed: {}", e.getMessage(), e);
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> suggest(String prefix) {
        try {
            Set<String> keys = redisTemplate.keys(SUGGEST_KEY + "*");
            if (keys == null || keys.isEmpty()) {
                return new ArrayList<>();
            }

            List<String> suggestions = new ArrayList<>();
            for (String key : keys) {
                List<Object> values = redisTemplate.opsForList().range(key, 0, -1);
                if (values != null) {
                    for (Object value : values) {
                        String title = value.toString();
                        if (title.toLowerCase().startsWith(prefix.toLowerCase())) {
                            suggestions.add(title);
                        }
                    }
                }
            }

            return suggestions.stream().distinct().limit(10).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Suggest failed: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Long> getStatistics() {
        try {
            ensureIndexExists();
            CountRequest request = CountRequest.of(c -> c.index(INDEX_NAME));
            long totalIndexed = elasticsearchClient.count(request).count();

            Map<String, Long> stats = new HashMap<>();
            stats.put("totalIndexed", totalIndexed);
            stats.put("totalSearches", 0L);
            return stats;
        } catch (Exception e) {
            log.error("Failed to get statistics: {}", e.getMessage(), e);
            Map<String, Long> stats = new HashMap<>();
            stats.put("totalIndexed", 0L);
            stats.put("totalSearches", 0L);
            return stats;
        }
    }

    private void ensureIndexExists() {
        try {
            boolean exists = elasticsearchClient.indices().exists(
                    ExistsRequest.of(e -> e.index(INDEX_NAME))
            ).value();

            if (!exists) {
                elasticsearchClient.indices().create(
                        CreateIndexRequest.of(c -> c.index(INDEX_NAME))
                );
                log.info("Created Elasticsearch index: {}", INDEX_NAME);
            }
        } catch (Exception e) {
            log.warn("Failed to ensure index exists: {}", e.getMessage());
        }
    }
}
