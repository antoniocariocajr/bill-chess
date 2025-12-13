package com.bill.bill_chess.infra.exception;

public class IllegalMoveException extends RuntimeException {
    public IllegalMoveException(String message) {
        super(message);
    }
}
