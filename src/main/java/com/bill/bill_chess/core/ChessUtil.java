package com.bill.bill_chess.core;

import com.bill.bill_chess.domain.model.Board;
import com.bill.bill_chess.domain.model.Move;
import com.bill.bill_chess.domain.model.Piece;
import com.bill.bill_chess.domain.model.Position;
import com.bill.bill_chess.infra.exception.IllegalMoveException;

import java.util.Optional;

public class ChessUtil {

    public static Board makeMove(Board board, Move move) {
        Optional<Piece> pieceOptional = board.pieceAt(move.from());
        if (pieceOptional.isEmpty())
            throw new IllegalMoveException("Piece not found!");
        if (!pieceOptional.get().isKing()) {
            if(pieceOptional.get().isPawn()&& move.promotion().isPresent()){
                board.doMove(move);
                return board;
            }
            board.doMove(move);
            return board;
        } else if (move.to().file() == move.from().file() + 2) {
            Optional<Piece> rookOptional = board.pieceAt(Position.of(move.from().rank(), move.from().file() + 3));
            if (rookOptional.isEmpty())
                throw new IllegalMoveException("Rook not found!");
            Move rookMove = Move.castle(
                    Position.of(move.from().rank(), move.from().file() + 3),
                    Position.of(move.to().rank(), move.to().file() - 1),
                    rookOptional.get());
            board.doMove(move);
            board.doMove(rookMove);
            return board;
        } else if (move.to().file() == move.from().file() - 2) {
            Optional<Piece> rookOptional = board.pieceAt(Position.of(move.from().rank(), move.from().file() - 4));
            if (rookOptional.isEmpty())
                throw new IllegalMoveException("Rook not found!");
            Move rookMove = Move.castle(
                    Position.of(move.from().rank(), move.from().file() - 4),
                    Position.of(move.to().rank(), move.to().file() + 1),
                    rookOptional.get());
            board.doMove(move);
            board.doMove(rookMove);
            return board;
        }
        board.doMove(move);
        return board;
    }

}
