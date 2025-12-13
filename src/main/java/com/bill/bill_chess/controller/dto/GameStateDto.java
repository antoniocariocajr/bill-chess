package com.bill.bill_chess.dto;

public record GameStateDto(
                String id,
                String fen,
                String activeColor, // "w" ou "b"
                String status, // IN_PROGRESS, CHECKMATE, STALEMATE
                boolean inCheck,
                String lastMoveUci, // e2e4
                boolean botNext // true -> front dispara bot move
) {
}
