package com.docman.search.service.impl;

import com.docman.search.entity.DocumentIndex;
import com.docman.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SEARCH_CACHE_KEY = "search:cache:";
    private static final String SUGGEST_KEY = "search:suggest:";

    @Override
    public void indexDocument(DocumentIndex document) {
        if (document.getIndexedAt() == null) {
            document.setIndexedAt(LocalDateTime.now());
        }
        if (document.getStatus() == null) {
            document.setStatus("ACTIVE");
        }

        elasticsearchTemplate.save(document);

        String suggestKey = SUGGEST_KEY + document.getDocumentId();
        redisTemplate.opsForList().rightPush(suggestKey, document.getTitle());

        log.info("Indexed document: {}", document.getDocumentId());
    }

    @Override
    public void updateIndex(DocumentIndex document) {
        document.setIndexedAt(LocalDateTime.now());
        elasticsearchTemplate.save(document);

        log.info("Updated index for document: {}", document.getDocumentId());
    }

    @Override
    public void deleteIndex(String documentId) {
        DocumentIndex document = new DocumentIndex();
        document.setDocumentId(documentId);
        document.setStatus("DELETED");

        elasticsearchTemplate.save(document);

        String suggestKey = SUGGEST_KEY + documentId;
        redisTemplate.delete(suggestKey);

        log.info("Deleted index for document: {}", documentId);
    }

    @Override
    public List<DocumentIndex> search(String keyword, int page, int size) {
        String cacheKey = SEARCH_CACHE_KEY + keyword + ":" + page + ":" + size;

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (List<DocumentIndex>) cached;
        }

        Criteria criteria = new Criteria("content").matches(keyword)
                .or(new Criteria("title").matches(keyword))
                .or(new Criteria("keywords").matches(keyword));

        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(org.springframework.data.domain.PageRequest.of(page, size));

        SearchHits<DocumentIndex> hits = elasticsearchTemplate.search(query, DocumentIndex.class);

        List<DocumentIndex> results = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        redisTemplate.opsForValue().set(cacheKey, results);

        return results;
    }

    @Override
    public List<String> suggest(String prefix) {
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
    }

    @Override
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalIndexed", 0L);
        stats.put("totalSearches", 0L);
        return stats;
    }
}
