package com.bill.bill_chess.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.enums.PieceType;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record Board(Piece[][] squares, List<Move> history) {

    public Board(Piece[][] squares, List<Move> history) {
        this.squares = squares;
        this.history = new ArrayList<>(history);
    }

    public static Board create() {
        Piece[][] squares = new Piece[9][8];
        initializeBoard(squares);
        return new Board(squares, new ArrayList<>());
    }

    public static Board copy(Board board) {
        Piece[][] newSquares = new Piece[9][8];
        for (int r = 0; r < 9; r++) {
            System.arraycopy(board.squares()[r], 0, newSquares[r], 0, 8);
        }
        return new Board(newSquares, board.history());
    }

    public static Board fromFen(String fenBoard, List<Move> history) {

        Piece[][] squares = new Piece[9][8];
        String[] rankStr = fenBoard.split("/");
        for (int r = 0; r <= 7; r++) {
            String row = rankStr[7 - r];
            int file = 0;
            for (char c : row.toCharArray()) {
                if (Character.isDigit(c)) {
                    file += c - '0';
                } else {
                    squares[r+1][file] = Piece.fromUnicode(String.valueOf(c));
                    file++;
                }
            }
        }

        return new Board(squares, history);
    }

    private static void initializeBoard(Piece[][] squares) {
        for (int rank = 0; rank < 9; rank++) {
            for (int file = 0; file < 8; file++) {
                squares[rank][file] = null;
            }
        }
        setupBackRank(squares, 1, Color.WHITE);
        setupPawns(squares, 2, Color.WHITE);
        setupBackRank(squares, 8, Color.BLACK);
        setupPawns(squares, 7, Color.BLACK);
    }

    public Optional<Piece> pieceAt(Position position) {
        return Optional.ofNullable(squares[position.rank()][position.file()]);
    }

    public void doMove(Move move) {
        Position to = move.to();
        Position from = move.from();
        squares[to.rank()][to.file()] = squares[from.rank()][from.file()];
        squares[from.rank()][from.file()] = null;
        this.history.addLast(move);
    }

    public void undoMove() {
        if(history().isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"List empty");
        Move lastMove = history.removeLast();
        Position from = lastMove.from();
        Position to = lastMove.to();
        squares[from.rank()][from.file()] = squares[to.rank()][to.file()];
        squares[to.rank()][to.file()] = lastMove.captured().orElse(null);
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
