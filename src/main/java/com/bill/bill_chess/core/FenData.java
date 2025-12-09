package com.bill.bill_chess.core;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.bill.bill_chess.domain.enums.CastleRight;
import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.enums.GameStatus;
import com.bill.bill_chess.domain.enums.PieceType;
import com.bill.bill_chess.domain.model.Board;
import com.bill.bill_chess.domain.model.Move;
import com.bill.bill_chess.domain.model.Piece;
import com.bill.bill_chess.domain.model.Position;

public record FenData(Board board, Color active, Set<CastleRight> rights,
        Position enPassant, int halfMove, int fullMove) {

    public boolean isInCheck(List<Move> knightMoves) {
        // 1) ache onde está o rei
        Position king = null;
        outer: for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Position position = new Position(rank, file);
                Optional<Piece> optPiece = board.pieceAt(position);
                if (optPiece.isPresent() && optPiece.get().getType() == PieceType.KING
                        && optPiece.get().getColor() == active) {
                    king = position;
                    break outer;
                }
            }
        }
        if (king == null)
            return false; // tabuleiro inválido

        // 2) verifique se alguma peça inimiga ataca a casa
        return isSquareAttacked(king, active.opposite(), knightMoves);
    }

    /** Classifica a situação da partida a partir de uma dada posição. */
    public GameStatus classify(List<Move> legal, List<Move> knightMoves) {

        if (legal.isEmpty()) {
            if (isInCheck(knightMoves)) {
                return active == Color.WHITE ? GameStatus.WHITE_WINS
                        : GameStatus.BLACK_WINS;
            }
            return GameStatus.STALEMATE;
        }
        return GameStatus.IN_PROGRESS;
    }

    /** Verifica se a casa <sq> é atacada por PEÇAS da cor <attacker>. */
    private boolean isSquareAttacked(Position sq, Color attacker, List<Move> knightMoves) {
        // peões
        int dir = attacker == Color.WHITE ? -1 : 1;
        for (int df : new int[] { -1, 1 }) {
            int pr = sq.getRank() + dir, pf = sq.getFile() + df;
            Position position = new Position(pr, pf);
            if (position.isValid()) {
                Optional<Piece> piece = board.pieceAt(position);
                if (piece.isPresent() && piece.get().getType() == PieceType.PAWN && piece.get().getColor() == attacker)
                    return true;
            }
        }
        // cavalo
        for (Move m : knightMoves) {
            Optional<Piece> p = board.pieceAt(Position.fromNotation(m.getTo()));
            if (p.isPresent() && p.get().getType() == PieceType.KNIGHT && p.get().getColor() == attacker)
                return true;
        }
        // sliding (rei, dama, torre, bispo)
        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 }, { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } };
        for (int[] d : dirs) {
            int r = sq.getRank() + d[0], f = sq.getFile() + d[1];
            Position position = new Position(r, f);
            while (position.isValid()) {
                Optional<Piece> op = board.pieceAt(position);
                if (op.isPresent()) {
                    Piece pc = op.get();
                    if (pc.getColor() != attacker)
                        break;
                    // verifica se a peça ataca ao longo dessa reta
                    boolean straight = (d[0] == 0 || d[1] == 0);
                    if (pc.getType() == PieceType.QUEEN ||
                            (straight && pc.getType() == PieceType.ROOK) ||
                            (!straight && pc.getType() == PieceType.BISHOP))
                        return true;
                    // rei ataca só a primeira casa
                    if (pc.getType() == PieceType.KING && Math.abs(position.getRank() - sq.getRank()) <= 1
                            && Math.abs(position.getFile() - sq.getFile()) <= 1)
                        return true;
                    break; // bloqueio
                }
                r += d[0];
                f += d[1];
            }
        }
        return false;
    }
}
