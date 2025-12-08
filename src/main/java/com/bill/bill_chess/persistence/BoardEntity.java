package com.bill.bill_chess.persistence;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "boards")
public record BoardEntity(
                @Id String id,
                String fenBoard, // posição
                String activeColor, // "w" ou "b"
                String castlingRights, // "KQkq" ou "-"
                String enPassantSquare, // "e3" ou "-"
                int halfMoveClock,
                int fullMoveNumber,
                List<String> moves, // histórico UCI
                Instant createdAt,
                Instant updatedAt) {

        public String fen() {
                return fenBoard + " " + activeColor + " " + castlingRights + " " + enPassantSquare + " " + halfMoveClock
                                + " " + fullMoveNumber;
        }
}
