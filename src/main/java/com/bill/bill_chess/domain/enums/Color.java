package com.bill.bill_chess.domain.enums;

public enum Color {
    WHITE,
    BLACK;

    public boolean isWhite() {
        return this == WHITE;
    }

    public boolean isBlack() {
        return this == BLACK;
    }

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

}
