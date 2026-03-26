package com.docman.standalone.application;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import io.minio.MinioClient;
import redis.clients.jedis.Jedis;

/**
 * DocMan Standalone Application
 * All-in-One JAR containing all services
 */
@SpringBootApplication(scanBasePackages = "com.docman")
@MapperScan({
    "com.docman.document.repository",
    "com.docman.user.repository",
    "com.docman.storage.repository",
    "com.docman.search.repository",
    "com.docman.version.repository",
    "com.docman.classification.repository",
    "com.docman.approval.repository",
    "com.docman.subscription.repository",
    "com.docman.collaboration.repository",
    "com.docman.cad.repository",
    "com.docman.system.repository"
})
public class StandaloneApplication {

    private static final Logger log = LoggerFactory.getLogger(StandaloneApplication.class);

    // Middleware connection config from application.yml
    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/docman}")
    private String mysqlUrl;

    @Value("${spring.datasource.username:root}")
    private String mysqlUsername;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${minio.endpoint:http://localhost:9000}")
    private String minioEndpoint;

    @Value("${minio.access-key:minioadmin}")
    private String minioAccessKey;

    @Value("${minio.secret-key:minioadmin}")
    private String minioSecretKey;

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUri;

    public static void main(String[] args) {
        printStartupBanner(args);
        SpringApplication.run(StandaloneApplication.class, args);
    }

    private static void printStartupBanner(String[] args) {
        log.info("");
        log.info("===========================================");
        log.info("  DocMan Standalone - Starting");
        log.info("===========================================");
        log.info("");
    }

    @Bean
    @Order(1)
    public CommandLineRunner healthChecker() {
        return args -> {
            printMiddlewareInfo();
            checkRedis();
            checkMinIO();
            checkElasticsearch();
            printStartedBanner();
        };
    }

    private void printMiddlewareInfo() {
        log.info("-------------------------------------------");
        log.info("  Middleware Configuration");
        log.info("-------------------------------------------");
        log.info("MySQL:        {}", mysqlUrl);
        log.info("Redis:        {}:{}", redisHost, redisPort);
        log.info("MinIO:        {}", minioEndpoint);
        log.info("Elasticsearch: {}", elasticsearchUri);
        log.info("");
    }

    private void checkRedis() {
        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            if (redisPassword != null && !redisPassword.isEmpty()) {
                jedis.auth(redisPassword);
            }
            jedis.ping();
            log.info("[Redis]         Connection: OK");
        } catch (Exception e) {
            log.warn("[Redis]         Connection: FAILED - {}", e.getMessage());
        }
    }

    private void checkMinIO() {
        try {
            MinioClient minioClient = MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(minioAccessKey, minioSecretKey)
                .build();
            minioClient.listBuckets();
            log.info("[MinIO]         Connection: OK");
        } catch (Exception e) {
            log.warn("[MinIO]         Connection: FAILED - {}", e.getMessage());
        }
    }

    private void checkElasticsearch() {
        try {
            java.net.URI uri = new java.net.URI(elasticsearchUri);
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                log.info("[Elasticsearch] Connection: OK");
            } else {
                log.warn("[Elasticsearch] Connection: FAILED - HTTP {}", response.statusCode());
            }
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
