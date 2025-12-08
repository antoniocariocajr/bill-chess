package com.bill.bill_chess.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.enums.PieceType;

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
public class Piece {

    @Id
    private String id;

    private PieceType type;

    private Color color;

    @DBRef
    private Position position;

    @Builder.Default
    private boolean hasMoved = false;

    @Builder.Default
    private boolean isCaptured = false;

    public Piece(PieceType type, Color color) {
        this.type = type;
        this.color = color;
    }

    public Piece(PieceType type, Color color, Position position) {
        this(type, color);
        this.position = position;
    }

    public String getSymbol() {
        String symbol = type.getSymbol();
        return color == Color.WHITE ? symbol : symbol.toLowerCase();
    }

}
