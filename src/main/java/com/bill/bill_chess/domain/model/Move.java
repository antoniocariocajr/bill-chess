package com.bill.bill_chess.domain.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document
public class Move {
    @Id
    private String id;
    @DBRef
    private Position from;
    @DBRef
    private Position to;
    @DBRef
    private Piece piece;
    @DBRef
    private Piece capturedPiece;

    @Builder.Default
    private boolean isCastling = false;

    @Builder.Default
    private boolean isEnPassant = false;

    @Builder.Default
    private boolean isPawnPromotion = false;

    @Builder.Default
    private boolean isCheck = false;

    @Builder.Default
    private boolean isCheckmate = false;

    @Builder.Default
    private boolean isStalemate = false;

    @CreatedDate
    @Builder.Default
    private Instant createdAt = Instant.now();

    public Move(Position from, Position to) {
        this.from = from;
        this.to = to;
    }

    public Move(Position from, Position to, Piece piece) {
        this(from, to);
        this.piece = piece;
    }

    public Move(Position from, Position to, Piece piece, Piece capturedPiece) {
        this(from, to);
        this.piece = piece;
        this.capturedPiece = capturedPiece;
    }

    public String toNotation() {
        return from.toNotation() + to.toNotation();
    }
}
