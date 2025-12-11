package com.bill.bill_chess.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "boards")
public record ChessEntity(
                @Id String id,
                String fenBoard, // posição
                String activeColor, // "w" ou "b"
                String playerBotColor, // "w" ou "b"
                String castlingRights, // "KQkq" ou "-"
                String enPassantSquare, // "e3" ou "-"
                int halfMoveClock,
                int fullMoveNumber,
                boolean inCheck,
                String status,
                List<String> moves, // histórico UCI
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
                                "w", "b", "KQkq", "-", 0, 1,
                                false,
                                "IN_PROGRESS",
                                new ArrayList<>(),
                                Instant.now(),
                                Instant.now());
        }
        public boolean isTurnBot(){
            return activeColor().equals(playerBotColor());
        }
}
