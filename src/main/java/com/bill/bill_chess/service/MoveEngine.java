package com.bill.bill_chess.service;

import reactor.core.publisher.Mono;

public interface MoveEngine {
    Mono<String> bestMove(String fen, int depth);
}
