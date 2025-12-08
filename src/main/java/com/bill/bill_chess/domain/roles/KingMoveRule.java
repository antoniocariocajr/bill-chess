package com.bill.bill_chess.domain.roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.bill.bill_chess.domain.enums.PieceType;
import com.bill.bill_chess.domain.model.Board;
import com.bill.bill_chess.domain.model.Move;
import com.bill.bill_chess.domain.model.Piece;
import com.bill.bill_chess.domain.model.Position;

@Component
public class KingMoveRule implements MoveRule {

    @Override
    public List<Move> getPossibleMoves(Board board, Position from) {
        List<Move> moves = new ArrayList<>();
        Optional<Piece> king = board.pieceAt(from);

        if (king.isEmpty()) {
            return moves;
        }

        // All 8 directions (one square)
        int[][] directions = {
                { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 },
                { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 }
        };

        for (int[] dir : directions) {
            int rank = from.getRank() + dir[0];
            int file = from.getFile() + dir[1];
            Position pos = new Position(rank, file);

            if (!pos.isValid()) {
                continue;
            }
            // check needed
            if (board.pieceAt(pos).isPresent()) {
                moves.add(Move.quiet(from, pos));
            } else if (board.isEnemyPiece(pos, king.get().getColor())) {
                moves.add(Move.capture(from, pos, board.pieceAt(pos).get()));
            }
        }
        boolean isHasMovedKing = board.getHistory().stream()
                .anyMatch(m -> m.getFrom().equals(from));
        // Castling check needed
        if (!isHasMovedKing) {
            // Kingside castling
            Position rookPos = new Position(from.getRank(), 7);
            boolean isHasMovedRook = board.getHistory().stream()
                    .anyMatch(m -> m.getFrom().equals(rookPos));
            Optional<Piece> rook = board.pieceAt(rookPos);
            if (rook.isPresent() && rook.get().getType() == PieceType.ROOK && !isHasMovedRook) {
                // Check if squares between king and rook are empty
                if (!board.pieceAt(new Position(from.getRank(), 5)).isPresent()
                        && !board.pieceAt(new Position(from.getRank(), 6)).isPresent()) {
                    moves.add(Move.castle(from, new Position(from.getRank(), 6)));
                }
            }

            // Queenside castling
            Position rookPosQ = new Position(from.getRank(), 0);
            boolean isHasMovedRookQ = board.getHistory().stream()
                    .anyMatch(m -> m.getFrom().equals(rookPosQ));
            rook = board.pieceAt(rookPosQ);
            if (rook.isPresent() && rook.get().getType() == PieceType.ROOK && !isHasMovedRookQ) {
                // Check if squares between king and rook are empty
                if (!board.pieceAt(new Position(from.getRank(), 1)).isPresent()
                        && !board.pieceAt(new Position(from.getRank(), 2)).isPresent()
                        && !board.pieceAt(new Position(from.getRank(), 3)).isPresent()) {
                    moves.add(Move.castle(from, new Position(from.getRank(), 2)));
                }
            }
        }

        return moves;
    }

    @Override
    public boolean isValidMove(Board board, Move move) {
        Optional<Piece> king = board.pieceAt(Position.fromNotation(move.getFrom()));
        if (king.isEmpty()) {
            return false;
        }

        List<Move> possibleMoves = getPossibleMoves(board, Position.fromNotation(move.getFrom()));
        return possibleMoves.stream()
                .anyMatch(m -> m.getTo().equals(move.getTo()));
    }
}
