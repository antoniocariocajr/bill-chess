package com.bill.bill_chess.domain.enums;

public enum GameStatus {
    /** game is in progress */
    IN_PROGRESS,

    /** game ended with White player winning */
    WHITE_WINS,

    /** game ended with Black player winning */
    BLACK_WINS,

    /** game ended in a draw */
    DRAW,

    /** game ended in stalemate */
    STALEMATE
}
