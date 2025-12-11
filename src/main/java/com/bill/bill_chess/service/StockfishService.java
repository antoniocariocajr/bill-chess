package com.bill.bill_chess.service;

import com.bill.bill_chess.core.ChessValidation;
import com.bill.bill_chess.core.MoveEngine;
import com.bill.bill_chess.config.StockfishProperties;
import com.bill.bill_chess.exception.ChessEngineException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
public class StockfishService implements MoveEngine {

    private final WebClient webClient;
    private final StockfishProperties props;
    private final Cache<String, String> cache;

    public StockfishService(WebClient.Builder builder,
                            StockfishProperties props) {
        this.props = props;
        this.webClient = builder
                .baseUrl(props.getBaseUrl())
                .filter(ExchangeFilterFunction.ofRequestProcessor(
                        req -> {
                            log.info("Chamando Stockfish: {}", req.url());
                            return Mono.just(req);
                        }))
                .build();

        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(props.getCache().getTtl())
                .maximumSize(props.getCache().getMaxSize())
                .recordStats()
                .build();
    }

    @Override
    public Mono<String> bestMove(String fen, int depth) {
        validate(fen, depth);

        String key = fen + "|" + depth;
        String cached = cache.getIfPresent(key);
        if (cached != null) {
            log.debug("Cache hit para key={}", key);
            return Mono.just(cached);
        }

        return callRemote(fen, depth)
                .doOnNext(move -> cache.put(key, move))
                .doOnError(ex -> log.error("Stockfish falhou para fen={}, depth={}", fen, depth, ex));
    }

    private Mono<String> callRemote(String fen, int depth) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        // path relativo, mas baseUrl já tem o esquema + host
                        .path(props.getPath())   // /api/stockfish.php
                        .queryParam("fen", fen)
                        .queryParam("depth", depth)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(props.getTimeout())
                .retryWhen(Retry.backoff(2, Duration.ofMillis(500)))
                .doOnError(err -> err.printStackTrace())  // log temporário
                .onErrorMap(ex -> new ChessEngineException("Falha ao obter movimento", ex));
    }

    private void validate(String fen, int depth) {
        if (depth <= 0 || depth > 20)
            throw new IllegalArgumentException("Profundidade deve estar entre 1 e 20");
        if (!ChessValidation.isValidFen(fen))
            throw new IllegalArgumentException("FEN inválido");
    }
}