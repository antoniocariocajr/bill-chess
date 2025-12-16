package com.bill.bill_chess.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Size;

@Document(collection = "boards")
public record ChessEntity(
                @Id String id,
                String fenBoard, // posição
                @Size(max = 1) String activeColor, // "w" ou "b"
                @Size(max = 4) String castlingRights, // "KQkq" ou "-"
                @Size(max = 2) String enPassantSquare, // "e3" ou "-"
                int halfMoveClock,
                int fullMoveNumber,
                boolean inCheck,
                @Size(max = 12) String status,
                List<MoveEntity> moves, // histórico UCI
                Instant createdAt,
                Instant updatedAt) {

        public String toFen() {
                return fenBoard + " " + activeColor + " " + castlingRights + " " + enPassantSquare + " " + halfMoveClock
                                + " " + fullMoveNumber;
        }

        public static ChessEntity initial() {
                return new ChessEntity(
                                null,
                                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR",
                                "w", "KQkq", "-", 0, 1,
                                false,
                                "IN_PROGRESS",
                                new ArrayList<>(),
                                Instant.now(),
                                Instant.now());
        }

}
