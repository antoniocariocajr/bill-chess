package com.bill.bill_chess.infra.exception;

public class ChessEngineException extends RuntimeException {
    public ChessEngineException(String message) {
        super(message);
    }

    public ChessEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
