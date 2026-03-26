package com.docman.standalone.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.boot.CommandLineRunner;
import io.minio.MinioClient;
import io.minio.params.bucket.BucketExistsArgs;

/**
 * DocMan Standalone Application
 * All-in-One JAR containing all services
 */
@SpringBootApplication(scanBasePackages = "com.docman")
public class StandaloneApplication {

    private static final Logger log = LoggerFactory.getLogger(StandaloneApplication.class);

    public static void main(String[] args) {
        printStartupBanner(args);
        SpringApplication.run(StandaloneApplication.class, args);
    }

    private static void printStartupBanner(String[] args) {
        log.info("");
        log.info("===========================================");
        log.info("  DocMan Standalone - Starting");
        log.info("===========================================");
        log.info("Gateway Port: 8080");
        log.info("MySQL:        localhost:3306/docman");
        log.info("Redis:         localhost:6379");
        log.info("MinIO:         localhost:9000");
        log.info("Elasticsearch: localhost:9200");
        log.info("");
    }

    @Bean
    @Order(1)
    public CommandLineRunner healthChecker(
            RedisConnectionFactory redisFactory,
            ElasticsearchOperations esOps) {
        return args -> {
            checkRedis(redisFactory);
            checkMinIO();
            checkElasticsearch(esOps);
            printStartedBanner();
        };
    }

    private void checkRedis(RedisConnectionFactory factory) {
        try {
            factory.getConnection().ping();
            log.info("[Redis]         Connection: OK");
        } catch (Exception e) {
            log.warn("[Redis]         Connection: FAILED - {}", e.getMessage());
        }
    }

    private void checkMinIO() {
        try {
            MinioClient minioClient = MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("minioadmin", "minioadmin")
                .build();
            minioClient.bucketExists(BucketExistsArgs.builder().bucket("docman-documents").build());
            log.info("[MinIO]         Connection: OK");
        } catch (Exception e) {
            log.warn("[MinIO]         Connection: FAILED - {}", e.getMessage());
        }
    }

    private void checkElasticsearch(ElasticsearchOperations ops) {
        try {
            ops.indexOps(Object.class).getMapping();
            log.info("[Elasticsearch] Connection: OK");
        } catch (Exception e) {
            log.warn("[Elasticsearch] Connection: FAILED - {}", e.getMessage());
        }
    }

    private void printStartedBanner() {
        log.info("");
        log.info("===========================================");
        log.info("  DocMan Standalone Started Successfully!");
        log.info("  Access URL: http://localhost:8080/docman");
        log.info("  API Docs:   http://localhost:8080/swagger-ui.html");
        log.info("===========================================");
        log.info("");
    }
}
