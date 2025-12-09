package com.bill.bill_chess.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Position {
    private final int rank; // 0-7
    private final int file; // 0-7 (a-h)

    public static Position fromNotation(String notation) {
        if (notation == null || notation.length() != 2) {
            throw new IllegalArgumentException("Invalid notation: " + notation);
        }

        char fileChar = notation.charAt(0);
        char rankChar = notation.charAt(1);

        int file = fileChar - 'a';
        int rank = Character.getNumericValue(rankChar);

        if (file < 0 || file > 7 || rank < 0 || rank > 7) {
            throw new IllegalArgumentException("Invalid notation: " + notation);
        }

        return new Position(rank, file);
    }

    public static Position of(int rank, int file) {
        return new Position(rank, file);
    }

    public String toNotation() {
        char fileChar = (char) ('a' + file);
        return "" + fileChar + rank;
    }

    public boolean isValid() {
        return rank >= 0 && rank <= 7 && file >= 0 && file <= 7;
    }

    public static boolean isValid(int rank, int file) {
        return rank >= 0 && rank <= 7 && file >= 0 && file <= 7;
    }

}
