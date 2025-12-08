package com.bill.bill_chess.domain.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.enums.PieceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Board {
    @Id
    private String id;

    @DBRef
    private Piece[][] board;

    @Builder.Default
    @CreatedDate
    private Instant createdAt = Instant.now();

    public Board() {
        this.board = new Piece[8][8];
        initializeBoard();
    }

    public void initializeBoard() {
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                board[rank][file] = null;
            }
        }
        setupBackRank(0, Color.WHITE);
        setupPawns(1, Color.WHITE);
        setupBackRank(7, Color.BLACK);
        setupPawns(6, Color.BLACK);
    }

    public void printBoard() {
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                Piece piece = board[rank][file];
                if (piece == null) {
                    System.out.print(". ");
                } else {
                    System.out.print(piece.getSymbol() + " ");
                }
            }
            System.out.println();
        }
    }

    public boolean isPieceAt(Position position) {
        return board[position.getRank()][position.getFile()] != null;
    }

    public Piece getPieceAt(Position position) {
        return board[position.getRank()][position.getFile()];
    }

    public void movePiece(Position from, Position to) {
        Piece piece = board[from.getRank()][from.getFile()];
        board[to.getRank()][to.getFile()] = piece;
        board[from.getRank()][from.getFile()] = null;
    }

    private void setupBackRank(int rank, Color color) {
        board[rank][0] = new Piece(PieceType.ROOK, color, new Position(rank, 0));
        board[rank][1] = new Piece(PieceType.KNIGHT, color, new Position(rank, 1));
        board[rank][2] = new Piece(PieceType.BISHOP, color, new Position(rank, 2));
        board[rank][3] = new Piece(PieceType.QUEEN, color, new Position(rank, 3));
        board[rank][4] = new Piece(PieceType.KING, color, new Position(rank, 4));
        board[rank][5] = new Piece(PieceType.BISHOP, color, new Position(rank, 5));
        board[rank][6] = new Piece(PieceType.KNIGHT, color, new Position(rank, 6));
        board[rank][7] = new Piece(PieceType.ROOK, color, new Position(rank, 7));
    }

    private void setupPawns(int rank, Color color) {
        for (int file = 0; file < 8; file++) {
            board[rank][file] = new Piece(PieceType.PAWN, color, new Position(rank, file));
        }
    }
}
