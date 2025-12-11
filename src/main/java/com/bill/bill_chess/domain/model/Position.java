package com.bill.bill_chess.domain.model;

public record Position(int rank, int file) {
    public static Position fromNotation(String notation) {
        if (notation == null || notation.length() != 2) {
            throw new IllegalArgumentException("Invalid notation: " + notation);
        }

        char fileChar = notation.charAt(0);
        char rankChar = notation.charAt(1);

        int file = fileChar - 'a';
        int rank = Character.getNumericValue(rankChar);

        if (file < 0 || file > 7 || rank < 1 || rank > 8) {
            throw new IllegalArgumentException("Invalid notation: " + notation);
        }

        return new Position(rank, file);
    }

    public static Position of(int rank, int file) {
        return new Position(rank, file);
    }

    public static boolean isValid(int rank, int file) {
        return rank >= 1 && rank <= 8 && file >= 0 && file <= 7;
    }

    public String toNotation() {
        char fileChar = (char) ('a' + file);
        return "" + fileChar + rank;
    }


}
