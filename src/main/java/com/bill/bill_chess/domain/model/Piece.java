package com.bill.bill_chess.domain.model;

import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.enums.PieceType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Piece {

    private final PieceType type;

    private final Color color;

    public static Piece of(Color color, PieceType type) {
        return new Piece(type, color);
    }

    public static Piece fromUnicode(String unicode) {
        return new Piece(PieceType.valueOf(unicode.toUpperCase()),
                Character.isUpperCase(unicode.charAt(0)) ? Color.WHITE : Color.BLACK);
    }

    public String getUnicode() {
        String unicode = type.getUnicode();
        return color == Color.WHITE ? unicode : unicode.toLowerCase();
    }

    public boolean isWhite() {
        return color == Color.WHITE;
    }

    public boolean isBlack() {
        return color == Color.BLACK;
    }

    public boolean isPawn() {
        return type == PieceType.PAWN;
    }

    public boolean isKnight() {
        return type == PieceType.KNIGHT;
    }

    public boolean isBishop() {
        return type == PieceType.BISHOP;
    }

    public boolean isRook() {
        return type == PieceType.ROOK;
    }

    public boolean isQueen() {
        return type == PieceType.QUEEN;
    }

    public boolean isKing() {
        return type == PieceType.KING;
    }

}
