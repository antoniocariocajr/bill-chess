package com.bill.bill_chess.exception;

public class ChessEngineException extends RuntimeException {
    public ChessEngineException(String message) {
        super(message);
    }

    public ChessEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
