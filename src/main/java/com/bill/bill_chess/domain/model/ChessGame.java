package com.bill.bill_chess.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.enums.GameStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Document
public class ChessGame {
    @Id
    private String id;

    @DBRef
    private Board board;

    // private Player whitePlayer;
    // private Player blackPlayer;

    @Builder.Default
    private Color correntColor = Color.WHITE;

    @Builder.Default
    private GameStatus status = GameStatus.IN_PROGRESS;

    @Builder.Default
    private boolean isGameOver = false;

    @DBRef
    @Builder.Default
    private List<Move> moveHistory = new ArrayList<>();

    @Builder.Default
    @CreatedDate
    private Instant createdAt = Instant.now();

    @Builder.Default
    @LastModifiedDate
    private Instant updatedAt = Instant.now();

    public ChessGame() {
        this.board = new Board();
    }

    public Move getLastMove() {
        if (moveHistory.isEmpty()) {
            return null;
        }
        return moveHistory.get(moveHistory.size() - 1);
    }

    public void makeMove(Move move) {
        moveHistory.add(move);
        board.movePiece(move.getFrom(), move.getTo());
        correntColor = correntColor == Color.WHITE ? Color.BLACK : Color.WHITE;
    }

    public void undoMove() {
        if (moveHistory.isEmpty()) {
            return;
        }
        Move move = moveHistory.remove(moveHistory.size() - 1);
        board.movePiece(move.getTo(), move.getFrom());
        correntColor = correntColor == Color.WHITE ? Color.BLACK : Color.WHITE;
    }

    public void endGame(GameStatus status) {
        this.status = status;
        this.isGameOver = true;
    }

    public void resetGame() {
        this.board.initializeBoard();
        this.correntColor = Color.WHITE;
        this.moveHistory.clear();
        this.status = GameStatus.IN_PROGRESS;
        this.isGameOver = false;
    }

    public void update() {
        this.updatedAt = Instant.now();
    }

    public int getMoveCount() {
        return moveHistory.size();
    }
}
