package com.bill.bill_chess.domain.roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.bill.bill_chess.domain.model.Board;
import com.bill.bill_chess.domain.model.Move;
import com.bill.bill_chess.domain.model.Piece;
import com.bill.bill_chess.domain.model.Position;

@Component
public class BishopMoveRule implements MoveRule {

    @Override
    public List<Move> getPossibleMoves(Board board, Position from) {
        List<Move> moves = new ArrayList<>();
        Piece bishop = board.pieceAt(from).orElse(null);

        if (bishop == null) {
            return moves;
        }

        // Diagonal directions: up-right, up-left, down-right, down-left
        int[][] directions = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };

        for (int[] dir : directions) {
            int rank = from.getRank();
            int file = from.getFile();

            while (true) {
                rank += dir[0];
                file += dir[1];

                Position pos = new Position(rank, file);
                if (!pos.isValid()) {
                    break;
                }

                if (board.pieceAt(pos).isEmpty()) {
                    moves.add(Move.quiet(from, pos));
                } else {
                    if (board.isEnemyPiece(pos, bishop.getColor())) {
                        moves.add(Move.capture(from, pos, board.pieceAt(pos).get()));
                        break;
                    }
                    break; // Can't move past a piece
                }
            }
        }

        return moves;
    }

    @Override
    public boolean isValidMove(Board board, Move move) {
        Optional<Piece> bishop = board.pieceAt(Position.fromNotation(move.getFrom()));
        if (bishop.isEmpty()) {
            return false;
        }

        List<Move> possibleMoves = getPossibleMoves(board, Position.fromNotation(move.getFrom()));
        return possibleMoves.stream()
                .anyMatch(m -> m.getTo().equals(move.getTo()));
    }
}
