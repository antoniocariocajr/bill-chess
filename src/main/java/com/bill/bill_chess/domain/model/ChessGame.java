package com.bill.bill_chess.domain.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
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

    private Board board;

    // private Player whitePlayer;
    // private Player blackPlayer;

    @Builder.Default
    private Color correntColor = Color.WHITE;

    @Builder.Default
    private GameStatus status = GameStatus.IN_PROGRESS;

    @Builder.Default
    private boolean isGameOver = false;

    @Builder.Default
    @CreatedDate
    private Instant createdAt = Instant.now();

    @Builder.Default
    @LastModifiedDate
    private Instant updatedAt = Instant.now();

    public ChessGame() {
        this.board = Board.create();
    }

    public void makeMove(Move move) {
        board.doMove(move);
        correntColor = correntColor == Color.WHITE ? Color.BLACK : Color.WHITE;
    }

    public void undoMove() {
        board.undoMove();
        correntColor = correntColor == Color.WHITE ? Color.BLACK : Color.WHITE;
    }

    public void endGame(GameStatus status) {
        this.status = status;
        this.isGameOver = true;
    }

    public void resetGame() {
        this.board = Board.create();
        this.correntColor = Color.WHITE;
        this.status = GameStatus.IN_PROGRESS;
        this.isGameOver = false;
        update();
    }

    public void update() {
        this.updatedAt = Instant.now();
    }

}
