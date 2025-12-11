package com.bill.bill_chess.domain.enums;

import lombok.Getter;

@Getter
public enum CastleRight {
    WHITE_KINGSIDE("K"), // Roque pequeno branco
    WHITE_QUEENSIDE("Q"), // Roque grande branco
    BLACK_KINGSIDE("k"), // Roque pequeno preto
    BLACK_QUEENSIDE("q"); // Roque grande preto

    private final String fenSymbol;

    CastleRight(String fenSymbol) {
        this.fenSymbol = fenSymbol;
    }

}
