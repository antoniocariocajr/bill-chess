package com.bill.bill_chess.infra.exception;

public class InvalidTurnException extends RuntimeException {
    public InvalidTurnException(String message) {
        super(message);
    }
}
