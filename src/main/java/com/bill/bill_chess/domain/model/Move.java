package com.bill.bill_chess.domain.model;

import java.util.Objects;
import java.util.Optional;

public record Move (
                Position from,
                Position to,
                Piece capturedPiece,
                Piece pawnPromotion,
                boolean isCastling,
                boolean isEnPassant,
                Piece pieceMoved){

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

    public static Move fromUci(String uci) {
        if (uci.length() < 4|| uci.length() > 5)  throw new IllegalArgumentException(uci);
        Position from = Position.fromNotation(uci.substring(0, 2));
        Position to = Position.fromNotation(uci.substring(2, 4));
        if (uci.length() == 5) {
            Piece pr = Piece.fromUnicode(uci.substring(4));
            return promotion(from, to, pr, null);
        }
        return quiet(from, to, null); // captura/en-passant/castling são descobertos pelo Board
    }

    public static Move fromUci(String uci, Piece pieceMoved) {
        if (uci.length() < 4|| uci.length() > 5)  throw new IllegalArgumentException(uci);
        Position from = Position.fromNotation(uci.substring(0, 2));
        Position to = Position.fromNotation(uci.substring(2, 4));
        if (uci.length() == 5) {
            Piece pr = Piece.fromUnicode(uci.substring(4));
            return promotion(from, to, pr, pieceMoved);
        }
        return quiet(from, to, pieceMoved); // captura/en-passant/castling são descobertos pelo Board
    }

    public Optional<Piece> captured() {
        return Optional.ofNullable(capturedPiece());
    }

    public Optional<Piece> promotion() {
        return Optional.ofNullable(pawnPromotion());
    }

    public Optional<Piece> moved() {
        return Optional.ofNullable(pieceMoved());
    }

    public String toUci() {
        StringBuilder sb = new StringBuilder();
        sb.append(from.toNotation()).append(to.toNotation());
        promotion().ifPresent(p -> sb.append(p.getUnicode().toLowerCase()));
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return Objects.equals(to.toNotation(), move.to.toNotation())
                && Objects.equals(from.toNotation(), move.from.toNotation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
