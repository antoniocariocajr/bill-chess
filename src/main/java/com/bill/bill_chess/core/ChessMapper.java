package com.bill.bill_chess.core;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.bill.bill_chess.domain.enums.CastleRight;
import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.model.Board;
import com.bill.bill_chess.domain.model.ChessGame;
import com.bill.bill_chess.domain.model.Move;
import com.bill.bill_chess.domain.model.Position;
import com.bill.bill_chess.dto.ChessDto;
import com.bill.bill_chess.persistence.ChessEntity;

@Component
public class ChessMapper {

    public ChessEntity toEntity(ChessGame game) {
        return FenUtils.toEntity(
                game.getId(),
                game.getBoard(),
                game.getCorrentColor(),
                game.getCastleRights(),
                game.getEnPassant(),
                game.getHalfMoveClock(),
                game.getFullMoveNumber(),
                game.getCreatedAt(),
                game.getUpdatedAt());
    }

    public ChessEntity toEntity(String id, FenData fenData) {
        return FenUtils.toEntity(
                id,
                fenData.board(),
                fenData.active(),
                fenData.rights(),
                fenData.enPassant(),
                fenData.halfMove(),
                fenData.fullMove(),
                Instant.now(),
                Instant.now());
    }

    public ChessGame toDomain(ChessEntity entity) {
        List<Move> moves = entity.moves().stream().map(m -> Move.fromUci(m)).toList();
        Color color = entity.activeColor() == "w" ? Color.WHITE : Color.BLACK;
        Set<CastleRight> rights = Set.of();
        for (char c : entity.castlingRights().toCharArray()) {
            switch (c) {
                case 'K' -> rights.add(CastleRight.WHITE_KINGSIDE);
                case 'Q' -> rights.add(CastleRight.WHITE_QUEENSIDE);
                case 'k' -> rights.add(CastleRight.BLACK_KINGSIDE);
                case 'q' -> rights.add(CastleRight.BLACK_QUEENSIDE);
            }
        }
        Position enPassant = entity.enPassantSquare() == "-" ? null : Position.fromNotation(entity.enPassantSquare());
        return ChessGame.builder()
                .id(entity.id())
                .board(Board.fromFen(entity.fenBoard(), moves))
                .correntColor(color)
                .castleRights(rights)
                .enPassant(enPassant)
                .halfMoveClock(entity.halfMoveClock())
                .fullMoveNumber(entity.fullMoveNumber())
                .createdAt(entity.createdAt())
                .updatedAt(entity.updatedAt())
                .build();
    }

    public ChessDto toDto(ChessEntity entity) {
        return new ChessDto(entity.id(), entity.fen());
    }

}
