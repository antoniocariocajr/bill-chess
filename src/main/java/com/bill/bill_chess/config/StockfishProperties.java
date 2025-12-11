package com.bill.bill_chess.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "stockfish")
@Data
public class StockfishProperties {
    private String baseUrl;
    private String path;
    private Duration timeout = Duration.ofSeconds(4);
    private Cache cache = new Cache();

    @Data
    public static class Cache {
        private Duration ttl = Duration.ofMinutes(10);
        private long maxSize = 1_000;
    }
}
