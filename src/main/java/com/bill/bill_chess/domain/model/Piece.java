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

    public static Piece fromUnicode(String unicode) {
        return new Piece(PieceType.valueOf(unicode.toUpperCase()),
                Character.isUpperCase(unicode.charAt(0)) ? Color.WHITE : Color.BLACK);
    }

    public String getUnicode() {
        String unicode = type.getUnicode();
        return color == Color.WHITE ? unicode : unicode.toLowerCase();
    }

}
