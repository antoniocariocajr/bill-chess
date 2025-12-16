package com.bill.bill_chess.service.util;

import com.bill.bill_chess.domain.enums.CastleRight;
import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.model.Board;
import com.bill.bill_chess.domain.model.Move;
import com.bill.bill_chess.domain.model.Piece;
import com.bill.bill_chess.domain.model.Position;
import com.bill.bill_chess.infra.exception.IllegalMoveException;

import java.util.Optional;
import java.util.Set;

import static com.bill.bill_chess.domain.enums.Color.WHITE;
import static com.bill.bill_chess.domain.enums.CastleRight.*;

public class ChessUtil {

    public static Board makeMove(Board board, Move move) {
        Optional<Piece> pieceOptional = board.pieceAt(move.from());
        if (pieceOptional.isEmpty())
            throw new IllegalMoveException("Piece not found!");
        if (!pieceOptional.get().isKing()) {
            if (pieceOptional.get().isPawn() && move.promotion().isPresent()) {
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

    public static String getPieceColor(Piece piece) {
        return piece.color() == WHITE ? "w" : "b";
    }

    public static Set<CastleRight> updateCastlingRights(Set<CastleRight> current, Move move) {
        if (move.pieceMoved().isKing()) {
            current.remove(move.pieceMoved().isWhite() ? WHITE_KINGSIDE : BLACK_KINGSIDE);
            current.remove(move.pieceMoved().isWhite() ? WHITE_QUEENSIDE : BLACK_QUEENSIDE);
            return current;
        }
        if (move.pieceMoved().isRook()) {
            Position from = move.from();
            int rank = from.rank();
            Color cor = move.pieceMoved().color();
            if (from.file() == 7 && rank == (cor.isWhite() ? 1 : 8)) {
                current.remove(move.pieceMoved().isWhite() ? WHITE_KINGSIDE : BLACK_KINGSIDE);
            }
            if (from.file() == 0 && rank == (cor.isWhite() ? 1 : 8)) {
                current.remove(move.pieceMoved().isWhite() ? WHITE_QUEENSIDE : BLACK_QUEENSIDE);
            }
            return current;
        }
        if (move.captured().isPresent() && move.captured().get().isRook()) {
            Position to = move.to();
            int rank = to.rank();
            Color cor = move.captured().get().color();
            if (to.file() == 7 && rank == (cor.isWhite() ? 1 : 8)) {
                current.remove(move.captured().get().isWhite() ? WHITE_KINGSIDE : BLACK_KINGSIDE);
            }
            if (to.file() == 0 && rank == (cor.isWhite() ? 1 : 8)) {
                current.remove(move.captured().get().isWhite() ? WHITE_QUEENSIDE : BLACK_QUEENSIDE);
            }
        }
        return current;
    }

    public static Position updateEnPassant(Board board, Move move) {
        if (!move.pieceMoved().isPawn())
            return null;
        Position from = move.from();
        Position to = move.to();
        int dr = Math.abs(to.rank() - from.rank());
        if (dr == 2) {
            return Position.of((from.rank() + to.rank()) / 2, from.file());
        }
        return null;
    }
}