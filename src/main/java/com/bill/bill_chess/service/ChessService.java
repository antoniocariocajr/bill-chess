package com.bill.bill_chess.service;

import com.bill.bill_chess.controller.dto.GameStateDto;
import com.bill.bill_chess.controller.dto.LegalMovesDto;
import com.bill.bill_chess.controller.dto.MoveDto;

public interface ChessService {

    GameStateDto createGame();

    GameStateDto makeHumanMove(String gameId, MoveDto dto);

    GameStateDto makeBotMove(String gameId, int depth);

    GameStateDto findById(String gameId);

    LegalMovesDto getLegalMoves(String gameId, String square);

    String getBestMove(String gameId, int depth);

    GameStateDto undoMove(String gameId);

}
