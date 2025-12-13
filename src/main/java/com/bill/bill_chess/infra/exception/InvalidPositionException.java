package com.bill.bill_chess.infra.exception;

public class InvalidPositionException extends RuntimeException {
    public InvalidPositionException(String message) {
        super(message);
    }
}
