package com.bill.bill_chess.core;

import java.util.*;

import com.bill.bill_chess.domain.model.Board;
import com.bill.bill_chess.domain.model.Move;
import com.bill.bill_chess.domain.model.Position;
import com.bill.bill_chess.domain.model.Piece;
import com.bill.bill_chess.domain.enums.GameStatus;
import com.bill.bill_chess.domain.enums.PieceType;
import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.enums.CastleRight;

public final class RuleSet {

    private RuleSet() {
    }

    /* ================== API pública ================== */
    public static List<Move> generateLegal(Board board, Color colorSide, Set<CastleRight> rights,
            Position enPassant) {

        List<Move> pseudo = new ArrayList<>(100);
        // 1) Movimentos normais + capturas
        for (int rank = 0; rank < 8; rank++)
            for (int file = 0; file < 8; file++) {
                Position position = Position.of(rank, file);
                board.pieceAt(position).ifPresent(piece -> {
                    if (piece.getColor() == colorSide) {
                        pseudo.addAll(pseudoMoves(board, position, piece, enPassant));
                    }
                });
            }
        // 2) Roques
        pseudo.addAll(generateCastling(board, colorSide, rights));
        // 3) Filtra xeque
        List<Move> legal = new ArrayList<>(pseudo.size());
        for (Move move : pseudo) {
            Board copyBoard = board.copy();
            copyBoard.doMove(move);
            if (!isInCheck(copyBoard, colorSide))
                legal.add(move);
        }
        return legal;
    }

    public static boolean isInCheck(Board board, Color colorSide) {
        Position kingPosition = null;
        outer: for (int rank = 0; rank < 8; rank++)
            for (int file = 0; file < 8; file++) {
                Position position = Position.of(rank, file);
                Optional<Piece> optPiece = board.pieceAt(position);
                if (optPiece.isPresent() && optPiece.get().getType() == PieceType.KING
                        && optPiece.get().getColor() == colorSide) {
                    kingPosition = position;
                    break outer;
                }
            }
        if (kingPosition == null)
            return false;
        return isSquareAttacked(board, kingPosition, colorSide.opposite());
    }

    public static GameStatus classify(Board board, Color colorSide,
            Set<CastleRight> rights,
            Position enPassant) {
        List<Move> legal = generateLegal(board, colorSide, rights, enPassant);
        if (legal.isEmpty()) {
            if (isInCheck(board, colorSide))
                return colorSide == Color.WHITE ? GameStatus.BLACK_WINS : GameStatus.WHITE_WINS;
            return GameStatus.STALEMATE;
        }
        return GameStatus.IN_PROGRESS;
    }

    /* ================== Implementações ================== */
    private static List<Move> pseudoMoves(Board board, Position from, Piece piece, Position enPassant) {
        List<Move> list = new ArrayList<>(28);
        Color colorSide = piece.getColor();
        switch (piece.getType()) {
            case PAWN -> list.addAll(pawnMoves(board, from, colorSide, enPassant));
            case KNIGHT -> list.addAll(knightMoves(board, from, colorSide));
            case BISHOP -> list.addAll(sliding(board, from, colorSide, bishopDirs));
            case ROOK -> list.addAll(sliding(board, from, colorSide, rookDirs));
            case QUEEN -> list.addAll(sliding(board, from, colorSide, queenDirs));
            case KING -> list.addAll(kingMoves(board, from, colorSide));
        }
        return list;
    }

    private static List<Move> pawnMoves(Board board, Position from, Color colorSide, Position enPassant) {
        List<Move> moves = new ArrayList<>(8);
        int dir = colorSide == Color.WHITE ? 1 : -1;
        int start = colorSide == Color.WHITE ? 1 : 6;
        int newRank = from.getRank() + dir;
        if (Position.isValid(newRank, from.getFile())
                && board.pieceAt(Position.of(newRank, from.getFile())).isEmpty()) {
            addPawn(moves, from, Position.of(newRank, from.getFile()), colorSide, null);
            if (from.getRank() == start) {
                int newRank2 = from.getRank() + 2 * dir;
                if (board.pieceAt(Position.of(newRank2, from.getFile())).isEmpty())
                    moves.add(Move.quiet(from, Position.of(newRank2, from.getFile()), board.pieceAt(from).get()));
            }
        }
        for (int df : new int[] { -1, 1 }) {
            int newFile = from.getFile() + df;
            if (!Position.isValid(newRank, newFile))
                continue;
            Position to = Position.of(newRank, newFile);
            Optional<Piece> opposingPiece = board.pieceAt(to);
            if (opposingPiece.isPresent() && opposingPiece.get().getColor() != colorSide)
                addPawn(moves, from, to, colorSide, opposingPiece.get());
            if (enPassant != null && to.equals(enPassant)) {
                moves.add(Move.enPassant(from, to, board.pieceAt(enPassant).get(), board.pieceAt(from).get()));
            }
        }
        return moves;
    }

    private static void addPawn(List<Move> moves, Position from, Position to, Color colorSide, Piece capture) {
        if (to.getRank() == 0 || to.getRank() == 7) {
            for (PieceType pr : new PieceType[] { PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT })
                if (capture != null) {
                    moves.add(new Move(from, to, capture, Piece.of(colorSide, pr), false, false,
                            Piece.of(colorSide, pr)));
                } else {
                    moves.add(Move.promotion(from, to, Piece.of(colorSide, pr), Piece.of(colorSide, pr)));
                }
        } else {
            moves.add(capture != null ? Move.capture(from, to, capture, Piece.of(colorSide, PieceType.PAWN))
                    : Move.quiet(from, to, Piece.of(colorSide, PieceType.PAWN)));
        }
    }

    private static List<Move> knightMoves(Board board, Position from, Color colorSide) {
        int[] rankDirection = { -2, -2, -1, -1, 1, 1, 2, 2 };
        int[] fileDirection = { -1, 1, -2, 2, -2, 2, -1, 1 };
        return stepMoves(board, from, colorSide, rankDirection, fileDirection);
    }

    private static List<Move> kingMoves(Board board, Position from, Color colorSide) {
        int[] rankDirection = { -1, -1, -1, 0, 0, 1, 1, 1 };
        int[] fileDirection = { -1, 0, 1, -1, 1, -1, 0, 1 };
        return stepMoves(board, from, colorSide, rankDirection, fileDirection);
    }

    private static List<Move> stepMoves(Board board, Position from, Color colorSide, int[] rankDirection,
            int[] fileDirection) {
        List<Move> moves = new ArrayList<>(rankDirection.length);
        for (int i = 0; i < rankDirection.length; i++) {
            int rankTrajectory = from.getRank() + rankDirection[i];
            int fileTrajectory = from.getFile() + fileDirection[i];
            if (!Position.isValid(rankTrajectory, fileTrajectory))
                continue;
            Position to = Position.of(rankTrajectory, fileTrajectory);
            Optional<Piece> opposingPiece = board.pieceAt(to);
            if (opposingPiece.isEmpty())
                moves.add(Move.quiet(from, to, board.pieceAt(from).get()));
            else if (opposingPiece.get().getColor() != colorSide)
                moves.add(Move.capture(from, to, opposingPiece.orElse(null), board.pieceAt(from).get()));
        }
        return moves;
    }

    private static final int[][] bishopDirs = { { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } };
    private static final int[][] rookDirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
    private static final int[][] queenDirs = Arrays.copyOf(bishopDirs, 8);// Position 0-3 (Bishop)
    static {
        System.arraycopy(rookDirs, 0, queenDirs, 4, 4);// Position 4-7 (Rook)
    }

    private static List<Move> sliding(Board board, Position from, Color colorSide, int[][] directions) {
        List<Move> moves = new ArrayList<>(28);
        for (int[] direction : directions) {
            int rank = from.getRank(), file = from.getFile();
            while (true) {
                rank += direction[0];
                file += direction[1];
                if (!Position.isValid(rank, file))
                    break;
                Position to = Position.of(rank, file);
                Optional<Piece> opposingPiece = board.pieceAt(to);
                if (opposingPiece.isEmpty())
                    moves.add(Move.quiet(from, to, board.pieceAt(from).get()));
                else {
                    if (opposingPiece.get().getColor() != colorSide)
                        moves.add(Move.capture(from, to, opposingPiece.orElse(null), board.pieceAt(from).get()));
                    break;
                }
            }
        }
        return moves;
    }

    private static List<Move> generateCastling(Board board, Color colorSide, Set<CastleRight> rights) {
        List<Move> moves = new ArrayList<>(2);
        boolean ks = colorSide == Color.WHITE ? rights.contains(CastleRight.WHITE_KINGSIDE)
                : rights.contains(CastleRight.BLACK_KINGSIDE);
        boolean qs = colorSide == Color.WHITE ? rights.contains(CastleRight.WHITE_QUEENSIDE)
                : rights.contains(CastleRight.BLACK_QUEENSIDE);
        int rank = colorSide == Color.WHITE ? 0 : 7;
        Position king = Position.of(rank, 4);
        if (!isInCheck(board, colorSide)) {
            // Kingside
            if (ks && board.pieceAt(Position.of(rank, 5)).isEmpty()
                    && board.pieceAt(Position.of(rank, 6)).isEmpty()
                    && !isSquareAttacked(board, Position.of(rank, 5), colorSide.opposite())
                    && !isSquareAttacked(board, Position.of(rank, 6), colorSide.opposite())) {
                moves.add(Move.castle(king, Position.of(rank, 6), board.pieceAt(king).get()));
            }
            // Queenside
            if (qs && board.pieceAt(Position.of(rank, 3)).isEmpty()
                    && board.pieceAt(Position.of(rank, 2)).isEmpty()
                    && board.pieceAt(Position.of(rank, 1)).isEmpty()
                    && !isSquareAttacked(board, Position.of(rank, 3), colorSide.opposite())
                    && !isSquareAttacked(board, Position.of(rank, 2), colorSide.opposite())) {
                moves.add(Move.castle(king, Position.of(rank, 2), board.pieceAt(king).get()));
            }
        }
        return moves;
    }

    private static boolean isSquareAttacked(Board board, Position square, Color colorOpponent) {
        // Pawn
        int direction = colorOpponent == Color.WHITE ? -1 : 1;
        for (int fileDirection : new int[] { -1, 1 }) {
            int pseudoRank = square.getRank() + direction, pseudoFile = square.getFile() + fileDirection;
            if (Position.isValid(pseudoRank, pseudoFile)) {
                Optional<Piece> piece = board.pieceAt(Position.of(pseudoRank, pseudoFile));
                if (piece.isPresent())
                    if (piece.get().getType() == PieceType.PAWN && piece.get().getColor() == colorOpponent)
                        return true;
            }
        }
        // Knight
        for (Move mv : knightMoves(board, square, colorOpponent)) {
            Optional<Piece> piece = board.pieceAt(Position.fromNotation(mv.getTo()));
            if (piece.isPresent())
                if (piece.get().getType() == PieceType.KNIGHT && piece.get().getColor() == colorOpponent)
                    return true;
        }
        // Sliding (Bishop, Rook, Queen)
        for (int[] d : queenDirs) {
            int pseudoRank = square.getRank() + d[0], pseudoFile = square.getFile() + d[1];
            while (Position.isValid(pseudoRank, pseudoFile)) {
                Position to = Position.of(pseudoRank, pseudoFile);
                Optional<Piece> opposingPiece = board.pieceAt(to);
                if (opposingPiece.isPresent()) {
                    Piece piece = opposingPiece.get();
                    if (piece.getColor() == colorOpponent) {
                        if ((piece.getType() == PieceType.BISHOP || piece.getType() == PieceType.QUEEN)
                                && (d[0] != 0 && d[1] != 0))
                            return true;
                        if ((piece.getType() == PieceType.ROOK || piece.getType() == PieceType.QUEEN)
                                && (d[0] == 0 || d[1] == 0))
                            return true;
                    }
                    break;
                }
                pseudoRank += d[0];
                pseudoFile += d[1];
            }
        }
        // King
        for (Move move : kingMoves(board, square, colorOpponent)) {
            Optional<Piece> piece = board.pieceAt(Position.fromNotation(move.getTo()));
            if (piece.isPresent())
                if (piece.get().getType() == PieceType.KING && piece.get().getColor() == colorOpponent)
                    return true;
        }
        return false;
    }
}
