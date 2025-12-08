package com.bill.bill_chess.domain.model;

import java.util.List;
import java.util.Optional;

import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.enums.PieceType;

import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Board {
    private final Piece[][] squares;
    private final List<Move> history;

    public static Board create() {
        Piece[][] squares = new Piece[8][8];
        initializeBoard(squares);
        return new Board(squares, List.of());
    }

    private static void initializeBoard(Piece[][] squares) {
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                squares[rank][file] = null;
            }
        }
        setupBackRank(squares, 0, Color.WHITE);
        setupPawns(squares, 1, Color.WHITE);
        setupBackRank(squares, 7, Color.BLACK);
        setupPawns(squares, 6, Color.BLACK);
    }

    public Optional<Piece> pieceAt(Position position) {
        return Optional.ofNullable(squares[position.getRank()][position.getFile()]);
    }

    public void doMove(Move move) {
        Position to = Position.fromNotation(move.getTo());
        Position from = Position.fromNotation(move.getFrom());
        squares[to.getRank()][to.getFile()] = squares[from.getRank()][from.getFile()];
        squares[from.getRank()][from.getFile()] = null;
        history.add(move);
    }

    public void undoMove() {
        Move lastMove = history.remove(history.size() - 1);
        Position from = Position.fromNotation(lastMove.getFrom());
        Position to = Position.fromNotation(lastMove.getTo());
        squares[from.getRank()][from.getFile()] = squares[to.getRank()][to.getFile()];
        squares[to.getRank()][to.getFile()] = lastMove.captured().orElse(null);
    }

    public int getMoveCount() {
        return history.size();
    }

    public boolean isEnemyPiece(Position position, Color color) {
        Piece piece = pieceAt(position).orElse(null);
        return piece != null && piece.getColor() != color;
    }

    private static void setupBackRank(Piece[][] squares, int rank, Color color) {
        squares[rank][0] = new Piece(PieceType.ROOK, color);
        squares[rank][1] = new Piece(PieceType.KNIGHT, color);
        squares[rank][2] = new Piece(PieceType.BISHOP, color);
        squares[rank][3] = new Piece(PieceType.QUEEN, color);
        squares[rank][4] = new Piece(PieceType.KING, color);
        squares[rank][5] = new Piece(PieceType.BISHOP, color);
        squares[rank][6] = new Piece(PieceType.KNIGHT, color);
        squares[rank][7] = new Piece(PieceType.ROOK, color);
    }

    private static void setupPawns(Piece[][] squares, int rank, Color color) {
        for (int file = 0; file < 8; file++) {
            squares[rank][file] = new Piece(PieceType.PAWN, color);
        }
    }
}
