package com.bill.bill_chess.core;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.bill.bill_chess.domain.enums.GameStatus;
import com.bill.bill_chess.domain.model.*;
import org.springframework.stereotype.Component;

import com.bill.bill_chess.domain.enums.CastleRight;
import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.dto.GameStateDto;
import com.bill.bill_chess.persistence.ChessEntity;

@Component
public class ChessMapper {

    public ChessEntity toEntity(ChessGame game) {
        return toEntity(
                game.getId(),
                game.getBoard(),
                game.getActiveColor(),
                game.getPlayerBotColor(),
                game.getCastleRights(),
                game.getEnPassant(),
                game.getHalfMoveClock(),
                game.getFullMoveNumber(),
                game.isInCheck(),
                game.getStatus(),
                game.getCreatedAt(),
                game.getUpdatedAt());
    }

    public ChessEntity toEntity(
            String id,
            Board board,
            Color active,
            Color playerBot,
            Set<CastleRight> rights,
            Position enPassant,
            int halfMove,
            int fullMove,
            boolean inCheck,
            GameStatus status,
            Instant createdAt,
            Instant updatedAt) {

        StringBuilder boardFen = getBoardFen(board);
        List<String> listMoves = board.history().stream()
                .map(Move::toUci).toList();
        String setRights = rights.stream().map(CastleRight::getFenSymbol).collect(Collectors.joining());

        return new ChessEntity(
                id,
                boardFen.toString(),
                active.isWhite() ? "w" : "b",
                playerBot.isWhite() ? "w" : "b",
                setRights,
                enPassant == null ? "-" : enPassant.toNotation(),
                halfMove,
                fullMove,
                inCheck,
                status.toString().toUpperCase(),
                listMoves,
                createdAt,
                updatedAt);
    }

    public ChessGame toDomain(ChessEntity entity) {
        List<Move> moves = entity.moves().stream().map(Move::fromUci).toList();
        Color color = Objects.equals(entity.activeColor(), "w") ? Color.WHITE : Color.BLACK;
        Set<CastleRight> rights = new HashSet<>(Set.of());
        for (char c : entity.castlingRights().toCharArray()) {
            switch (c) {
                case 'K' -> rights.add(CastleRight.WHITE_KINGSIDE);
                case 'Q' -> rights.add(CastleRight.WHITE_QUEENSIDE);
                case 'k' -> rights.add(CastleRight.BLACK_KINGSIDE);
                case 'q' -> rights.add(CastleRight.BLACK_QUEENSIDE);
            }
        }
        System.out.println(entity.id());
        System.out.println(entity.toFen());
        Position enPassant = Objects.equals(entity.enPassantSquare(), "-") ? null : Position.fromNotation(entity.enPassantSquare());
        return ChessGame.builder()
                .id(entity.id())
                .board(Board.fromFen(entity.fenBoard(), moves))
                .activeColor(color)
                .playerBotColor(Objects.equals(entity.playerBotColor(), "w") ? Color.WHITE : Color.BLACK)
                .castleRights(rights)
                .enPassant(enPassant)
                .halfMoveClock(entity.halfMoveClock())
                .fullMoveNumber(entity.fullMoveNumber())
                .createdAt(entity.createdAt())
                .updatedAt(entity.updatedAt())
                .build();
    }

    public GameStateDto toGameStateDto(ChessEntity entity) {
        return new GameStateDto(
                entity.id(),
                entity.toFen(),
                entity.activeColor(),
                entity.status(),
                entity.inCheck(),
                entity.moves().isEmpty()?"-":entity.moves().getLast(),
                entity.activeColor().equals(entity.playerBotColor()));
    }

    private StringBuilder getBoardFen(Board board) {
        StringBuilder boardFen = new StringBuilder();
        for (int rank = 8; rank >= 1; rank--) {
            int empty = 0;
            for (int file = 0; file < 8; file++) {
                Optional<Piece> op = board.pieceAt(new Position(rank, file));
                if (op.isEmpty()) {
                    empty++;
                } else {
                    if (empty > 0) {
                        boardFen.append(empty);
                        empty = 0;
                    }
                    boardFen.append(op.get().getUnicode());
                }
            }
            if (empty > 0)
                boardFen.append(empty);
            if (rank > 1)
                boardFen.append('/');
        }
        return boardFen;
    }


}
