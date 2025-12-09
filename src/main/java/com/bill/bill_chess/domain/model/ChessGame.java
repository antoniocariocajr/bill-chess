package com.bill.bill_chess.domain.model;

import java.time.Instant;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.bill.bill_chess.domain.enums.CastleRight;
import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.enums.GameStatus;
import com.bill.bill_chess.persistence.ChessEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ChessGame {

    private String id;

    private Board board;

    // private Player whitePlayer;
    // private Player blackPlayer;

    @Builder.Default
    private Color correntColor = Color.WHITE;

    @Builder.Default
    private GameStatus status = GameStatus.IN_PROGRESS;

    @Builder.Default
    private Set<CastleRight> castleRights = Set.of();

    @Builder.Default
    private Position enPassant = null;

    @Builder.Default
    private int halfMoveClock = 0;

    @Builder.Default
    private int fullMoveNumber = 1;

    @Builder.Default
    @CreatedDate
    private Instant createdAt = Instant.now();

    @Builder.Default
    @LastModifiedDate
    private Instant updatedAt = Instant.now();

    public ChessGame() {
        this.board = Board.create();
    }

    public ChessGame(ChessEntity boardEntity) {

        this.id = boardEntity.id();
        this.board = Board.fromFen(boardEntity.fenBoard());
        this.correntColor = boardEntity.activeColor() == "w" ? Color.WHITE : Color.BLACK;
        Set<CastleRight> rights = Set.of();
        for (char c : boardEntity.castlingRights().toCharArray()) {
            switch (c) {
                case 'K' -> rights.add(CastleRight.WHITE_KINGSIDE);
                case 'Q' -> rights.add(CastleRight.WHITE_QUEENSIDE);
                case 'k' -> rights.add(CastleRight.BLACK_KINGSIDE);
                case 'q' -> rights.add(CastleRight.BLACK_QUEENSIDE);
            }
        }
        this.castleRights = rights;
        this.enPassant = boardEntity.enPassantSquare() == "-" ? null
                : Position.fromNotation(boardEntity.enPassantSquare());
        this.halfMoveClock = boardEntity.halfMoveClock();
        this.fullMoveNumber = boardEntity.fullMoveNumber();
        this.createdAt = boardEntity.createdAt();
        this.updatedAt = boardEntity.updatedAt();
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
    }

    public void resetGame() {
        this.board = Board.create();
        this.correntColor = Color.WHITE;
        this.status = GameStatus.IN_PROGRESS;
        update();
    }

    public void update() {
        this.updatedAt = Instant.now();
    }

}
