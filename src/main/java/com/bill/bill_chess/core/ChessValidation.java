package com.bill.bill_chess.core;

public final class ChessValidation {
    private ChessValidation(){}

    public static boolean isValidFen(String fen){
        String[] part = fen.split(" ");
        System.out.println("-1");
        if (part.length != 6) return false;
        System.out.println("0");
        if (!isValidBoard(part[0])) return false;
        System.out.println("1");
        if (!part[1].matches("[wb]")) return false;
        System.out.println("2");
        if (!isValidCastling(part[2])) return false;
        System.out.println("3");
        if (!part[3].matches("-|[a-h][36]")) return false;
        System.out.println("4");
        if (!part[4].matches("\\d+")) return false;
        System.out.println("5");
        return part[5].matches("\\d+");
    }
    private static boolean isValidBoard(String boardPart) {
        String[] ranks = boardPart.split("/");
        if (ranks.length != 8) return false;

        for (String rank : ranks) {
            int squares = 0;
            for (char c : rank.toCharArray()) {
                if (Character.isDigit(c)) {
                    squares += c - '0';
                } else if ("PNBRQKpnbrqk".indexOf(c) >= 0) {
                    squares += 1;
                } else {
                    return false; // caractere inválido
                }
            }
            if (squares != 8) return false;
        }
        return true;
    }
    private static boolean isValidCastling(String castling) {
        if (castling.equals("-")) return true;
        boolean k = false, q = false, K = false, Q = false;
        for (char c : castling.toCharArray()) {
            switch (c) {
                case 'K' -> { if (K) return false; K = true; }
                case 'Q' -> { if (Q) return false; Q = true; }
                case 'k' -> { if (k) return false; k = true; }
                case 'q' -> { if (q) return false; q = true; }
                default  -> {return false;}
            }
        }
        return true;
    }
}
