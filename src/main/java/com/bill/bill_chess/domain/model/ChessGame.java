package com.bill.bill_chess.domain.model;

import java.time.Instant;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.bill.bill_chess.domain.enums.CastleRight;
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
public class ChessGame {

    private String id;

    private Board board;

    @Builder.Default
    private Color activeColor = Color.WHITE;

    @Builder.Default
    private Color playerBotColor = Color.BLACK;

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
    boolean inCheck = false;

    @Builder.Default
    @CreatedDate
    private Instant createdAt = Instant.now();

    @Builder.Default
    @LastModifiedDate
    private Instant updatedAt = Instant.now();

}
