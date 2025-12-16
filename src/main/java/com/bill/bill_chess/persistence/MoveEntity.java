package com.bill.bill_chess.persistence;

import jakarta.validation.constraints.Size;

public record MoveEntity(
        @Size(max = 5) String uci,
        @Size(max = 1) String capturedPiece, // unicode
        @Size(max = 1) String promotion, // unicode
        @Size(max = 1) String pieceMoved, // unicode
        boolean isCastling,
        boolean isEnPassant) {

}
