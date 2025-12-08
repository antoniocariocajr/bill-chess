package com.bill.bill_chess.domain.roles;

import java.util.List;

import com.bill.bill_chess.domain.model.Board;
import com.bill.bill_chess.domain.model.Move;
import com.bill.bill_chess.domain.model.Position;

public interface MoveRule {
    List<Move> getPossibleMoves(Board board, Position from);

    boolean isValidMove(Board board, Move move);
}
