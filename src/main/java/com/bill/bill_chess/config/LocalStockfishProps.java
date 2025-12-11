package com.bill.bill_chess.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "stockfish.local")
public class LocalStockfishProps {
    private String exe = "stockfish";
}