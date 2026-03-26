package com.docman.standalone.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Simple Cache Manager for Standalone Mode
 * Uses in-memory cache instead of Redis
 */
@Configuration
@Profile("standalone")
public class StandaloneCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("document", "folder", "user");
    }
}
