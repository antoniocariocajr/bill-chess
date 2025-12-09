package com.bill.bill_chess.domain.model;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Move {

    private final String from;

    private final String to;

    private final Piece capturedPiece;

    private final Piece pawnPromotion;

    private final boolean isCastling;

    private final boolean isEnPassant;

    private final Piece pieceMoved;

    public Move(Position from, Position to, Piece capturedPiece, Piece pawnPromotion, boolean isCastling,
            boolean isEnPassant, Piece pieceMoved) {
        this.from = from.toNotation();
        this.to = to.toNotation();
        this.capturedPiece = capturedPiece;
        this.pawnPromotion = pawnPromotion;
        this.isCastling = isCastling;
        this.isEnPassant = isEnPassant;
        this.pieceMoved = pieceMoved;
    }

    public static Move quiet(Position from, Position to, Piece pieceMoved) {
        return new Move(from, to, null, null, false, false, pieceMoved);
    }

    public static Move capture(Position from, Position to, Piece capturedPiece, Piece pieceMoved) {
        return new Move(from, to, capturedPiece, null, false, false, pieceMoved);
    }

    public static Move promotion(Position from, Position to, Piece promoted, Piece pieceMoved) {
        return new Move(from, to, null, promoted, false, false, pieceMoved);
    }

    public static Move enPassant(Position from, Position to, Piece capturedPiece, Piece pieceMoved) {
        return new Move(from, to, capturedPiece, null, false, true, pieceMoved);
    }

    public static Move castle(Position from, Position to, Piece pieceMoved) {
        return new Move(from, to, null, null, true, false, pieceMoved);
    }

    public Optional<Piece> captured() {
        return Optional.ofNullable(capturedPiece);
    }

    public Optional<Piece> pawnPromotion() {
        return Optional.ofNullable(pawnPromotion);
    }

    public String toUci() {
        StringBuilder sb = new StringBuilder();
        sb.append(from).append(to);
        pawnPromotion().ifPresent(p -> sb.append(p.getUnicode().toLowerCase()));
        return sb.toString();
    }

    public static Move fromUci(String uci) {
        if (uci.length() < 4)
            throw new IllegalArgumentException(uci);
        Position from = Position.fromNotation(uci.substring(0, 2));
        Position to = Position.fromNotation(uci.substring(2, 4));
        if (uci.length() == 5) {
            Piece pr = Piece.fromUnicode(uci.substring(4));
            return promotion(from, to, pr, pr);
        }
        return quiet(from, to, null); // captura/en-passant/castling são descobertos pelo Board
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(from + "-" + to);
        if (isCastling)
            sb.append("(O-O)");
        if (isEnPassant)
            sb.append("(ep)");
        pawnPromotion().ifPresent(p -> sb.append("=").append(p));
        captured().ifPresent(p -> sb.append("x").append(p));
        return sb.toString();
    }
}
