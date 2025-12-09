package com.bill.bill_chess.core;

import java.util.Set;

import com.bill.bill_chess.domain.enums.CastleRight;
import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.model.Board;
import com.bill.bill_chess.domain.model.Position;

public record FenData(Board board, Color active, Set<CastleRight> rights,
        Position enPassant, int halfMove, int fullMove) {

}
