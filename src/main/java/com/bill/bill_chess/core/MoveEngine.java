package com.bill.bill_chess.core;

import reactor.core.publisher.Mono;

public interface MoveEngine {
    Mono<String> bestMove(String fen, int depth);
}
