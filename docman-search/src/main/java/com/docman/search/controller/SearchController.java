package com.docman.search.controller;

import com.docman.search.entity.DocumentIndex;
import com.docman.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/index")
    public ResponseEntity<Void> indexDocument(@RequestBody DocumentIndex document) {
        searchService.indexDocument(document);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/index")
    public ResponseEntity<Void> updateIndex(@RequestBody DocumentIndex document) {
        searchService.updateIndex(document);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/index/{documentId}")
    public ResponseEntity<Void> deleteIndex(@PathVariable String documentId) {
        searchService.deleteIndex(documentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<DocumentIndex>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(searchService.search(keyword, page, size));
    }

    @GetMapping("/suggest")
    public ResponseEntity<List<String>> suggest(@RequestParam String prefix) {
        return ResponseEntity.ok(searchService.suggest(prefix));
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        return ResponseEntity.ok(searchService.getStatistics());
    }
}
