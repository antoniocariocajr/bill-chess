package com.bill.bill_chess.service;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.bill.bill_chess.controller.dto.GameStateDto;
import com.bill.bill_chess.controller.dto.LegalMovesDto;
import com.bill.bill_chess.controller.dto.MoveDto;

public interface ChessService {

    GameStateDto createGame(JwtAuthenticationToken token);

    GameStateDto makeHumanMove(String gameId, MoveDto dto, JwtAuthenticationToken token);

    GameStateDto makeBotMove(String gameId, int depth, JwtAuthenticationToken token);

    GameStateDto findById(String gameId, JwtAuthenticationToken token);

    LegalMovesDto getLegalMoves(String gameId, String square);

    String getBestMove(String gameId, int depth);

    GameStateDto undoMove(String gameId, JwtAuthenticationToken token);

}
