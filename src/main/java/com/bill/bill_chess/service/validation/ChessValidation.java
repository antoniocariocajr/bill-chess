package com.bill.bill_chess.service.validation;

import java.util.Set;

import com.bill.bill_chess.controller.dto.MoveDto;
import com.bill.bill_chess.domain.enums.CastleRight;
import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.model.Board;
import com.bill.bill_chess.domain.model.Move;
import com.bill.bill_chess.domain.model.Position;
import com.bill.bill_chess.infra.constants.GameConstants;
import com.bill.bill_chess.infra.exception.IllegalMoveException;
import com.bill.bill_chess.infra.exception.InvalidTurnException;
import com.bill.bill_chess.service.rule.RuleSet;

public final class ChessValidation {
    private ChessValidation() {
    }

    public static boolean isValidFen(String fen) {
        String[] part = fen.split(" ");
        System.out.println("-1");
        if (part.length != 6)
            return false;
        System.out.println("0");
        if (!isValidBoard(part[0]))
            return false;
        System.out.println("1");
        if (!part[1].matches("[wb]"))
            return false;
        System.out.println("2");
        if (!isValidCastling(part[2]))
            return false;
        System.out.println("3");
        if (!part[3].matches("-|[a-h][36]"))
            return false;
        System.out.println("4");
        if (!part[4].matches("\\d+"))
            return false;
        System.out.println("5");
        return part[5].matches("\\d+");
    }

    public static void validateTurn(MoveDto dto, Color active) {
        if (!dto.color().equalsIgnoreCase(active.fen()))
            throw new InvalidTurnException(GameConstants.NOT_YOUR_TURN_MSG);
    }

    public static void validateLegality(Board board, Move move, Color active,
            Set<CastleRight> rights, Position enPassant) {
        boolean legal = RuleSet.generateLegal(board, active, rights, enPassant)
                .stream()
                .anyMatch(m -> m.equals(move));
        if (!legal)
            throw new IllegalMoveException("Illegal move: " + move.toUci());
    }

    public static boolean isValidBoard(String boardPart) {
        String[] ranks = boardPart.split("/");
        if (ranks.length != 8)
            return false;

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
            if (squares != 8)
                return false;
        }
        return true;
    }

    public static boolean isValidCastling(String castling) {
        if (castling.equals("-"))
            return true;
        boolean k = false, q = false, K = false, Q = false;
        for (char c : castling.toCharArray()) {
            switch (c) {
                case 'K' -> {
                    if (K)
                        return false;
                    K = true;
                }
                case 'Q' -> {
                    if (Q)
                        return false;
                    Q = true;
                }
                case 'k' -> {
                    if (k)
                        return false;
                    k = true;
                }
                case 'q' -> {
                    if (q)
                        return false;
                    q = true;
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }
}
