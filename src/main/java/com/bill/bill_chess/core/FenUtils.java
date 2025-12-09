package com.bill.bill_chess.core;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.bill.bill_chess.domain.enums.CastleRight;
import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.model.Board;
import com.bill.bill_chess.domain.model.Piece;
import com.bill.bill_chess.domain.model.Position;
import com.bill.bill_chess.persistence.ChessEntity;

/**
 * Converte Board ↔ FEN e extrai metadados (roque, en-passant).
 */
public final class FenUtils {

    private FenUtils() {
    }

    public static ChessEntity toEntity(
            String id,
            Board board,
            Color active,
            Set<CastleRight> rights,
            Position enPassant,
            int halfMove,
            int fullMove,
            Instant createdAt,
            Instant updatedAt) {
        StringBuilder boardFen = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            int empty = 0;
            for (int file = 0; file < 8; file++) {
                Optional<Piece> op = board.pieceAt(new Position(rank, file));
                if (op.isEmpty()) {
                    empty++;
                } else {
                    if (empty > 0) {
                        boardFen.append(empty);
                        empty = 0;
                    }
                    boardFen.append(op.get().getUnicode());
                }
            }
            if (empty > 0)
                boardFen.append(empty);
            if (rank > 0)
                boardFen.append('/');
        }

        List<String> moves = board.getHistory().stream()
                .map(move -> move.toUci()).toList();
        String setRights = rights.stream().map(CastleRight::getFenSymbol).collect(Collectors.joining());

        return new ChessEntity(
                id,
                boardFen.toString(),
                active == Color.WHITE ? "w" : "b",
                setRights,
                enPassant == null ? "-" : enPassant.toNotation(),
                halfMove,
                fullMove,
                moves,
                createdAt,
                updatedAt);
    }

    /* ========== Board → FEN ========== */
    public static String toFen(Board board, Color active, Set<CastleRight> rights,
            Position enPassant, int halfMove, int fullMove) {
        StringBuilder fen = new StringBuilder();
        // 1) Percorre ranks (8 → 1)
        for (int rank = 7; rank >= 0; rank--) {
            int empty = 0;
            for (int file = 0; file < 8; file++) {
                Optional<Piece> op = board.pieceAt(new Position(rank, file));
                if (op.isEmpty()) {
                    empty++;
                } else {
                    if (empty > 0) {
                        fen.append(empty);
                        empty = 0;
                    }
                    fen.append(op.get().getUnicode());
                }
            }
            if (empty > 0)
                fen.append(empty);
            if (rank > 0)
                fen.append('/');
        }
        // 2) Metadados
        fen.append(' ').append(active == Color.WHITE ? 'w' : 'b').append(' ');
        fen.append(rights.stream().map(CastleRight::getFenSymbol).collect(Collectors.joining())).append(' ');
        fen.append(enPassant == null ? '-' : enPassant.toNotation()).append(' ');
        fen.append(halfMove).append(' ').append(fullMove);
        return fen.toString();
    }

    /* ========== FEN → Board ========== */
    public static FenData fromFen(String fen) {
        String[] part = fen.split(" ");
        if (part.length != 6)
            throw new IllegalArgumentException("FEN incompleto");

        Piece[][] sq = new Piece[8][8];
        String[] rankStr = part[0].split("/");
        for (int r = 0; r < 8; r++) {
            String row = rankStr[7 - r];
            int file = 0;
            for (char c : row.toCharArray()) {
                if (Character.isDigit(c)) {
                    file += c - '0';
                } else {
                    sq[r][file] = Piece.fromUnicode(String.valueOf(c));
                    file++;
                }
            }
        }
        Color active = part[1].charAt(0) == 'w' ? Color.WHITE : Color.BLACK;
        Set<CastleRight> rights = Set.of();
        for (char c : part[2].toCharArray()) {
            switch (c) {
                case 'K' -> rights.add(CastleRight.WHITE_KINGSIDE);
                case 'Q' -> rights.add(CastleRight.WHITE_QUEENSIDE);
                case 'k' -> rights.add(CastleRight.BLACK_KINGSIDE);
                case 'q' -> rights.add(CastleRight.BLACK_QUEENSIDE);
            }
        }
        Position ep = "-".equals(part[3]) ? null : Position.fromNotation(part[3]);
        int half = Integer.parseInt(part[4]);
        int full = Integer.parseInt(part[5]);

        return new FenData(new Board(sq, List.of()), active, rights, ep, half, full);
    }

}
