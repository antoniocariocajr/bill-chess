package com.bill.bill_chess.domain.enums;

public enum Color {
    WHITE("w"),
    BLACK("b");

    private final String fen;

    Color(String fen) {
        this.fen = fen;
    }

    public String fen() {
        return fen;
    }

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
