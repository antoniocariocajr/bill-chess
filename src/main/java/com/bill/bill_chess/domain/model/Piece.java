package com.bill.bill_chess.domain.model;

import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.enums.PieceType;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record Piece(PieceType type, Color color) {

    public static Piece of(Color color, PieceType type) {
        return new Piece(type, color);
    }

    public static Piece fromUnicode(String unicode) {
        PieceType pieceType = switch (unicode.toUpperCase()) {
            case "P" -> PieceType.PAWN;
            case "R" -> PieceType.ROOK;
            case "N" -> PieceType.KNIGHT;
            case "B" -> PieceType.BISHOP;
            case "Q" -> PieceType.QUEEN;
            case "K" -> PieceType.KING;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unexpected value: " + unicode.toUpperCase());
        };
        Color colorUnicode = Character.isUpperCase(unicode.charAt(0)) ? Color.WHITE : Color.BLACK;
        return new Piece(pieceType, colorUnicode);
    }

    public String getUnicode() {
        String unicode = type.getName();
        return isWhite()? unicode : unicode.toLowerCase();
    }

    public boolean isWhite() {
        return color == Color.WHITE;
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
