package com.bill.bill_chess.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bill.bill_chess.dto.GameStateDto;
import com.bill.bill_chess.dto.LegalMovesDto;
import com.bill.bill_chess.dto.MoveDto;
import com.bill.bill_chess.service.ChessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/chess")
@RequiredArgsConstructor
public class ChessController {

    private final ChessService chessService;

    @PostMapping("/init")
    @Operation(summary = "init a new game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Game initialized successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public GameStateDto initGame() {
        return chessService.createGame();
    }

    @GetMapping("/{id}/legal-moves")
    @Operation(summary = "get legal moves for a specific square")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Legal moves retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.OK)
    public LegalMovesDto LegalMoves(@PathVariable String id, @RequestParam String square) {
        return chessService.getLegalMoves(id, square);
    }

    @GetMapping("/{id}")
    @Operation(summary = "get game state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game state retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @ResponseStatus(HttpStatus.OK)
    public GameStateDto getGame(@PathVariable String id) {
        return chessService.getGame(id);
    }

    @PostMapping("/{id}/move")
    @Operation(summary = "make a move")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Move made successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
            @ApiResponse(responseCode = "400", description = "Invalid move"),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @ResponseStatus(HttpStatus.OK)
    public GameStateDto move(@PathVariable String id, @RequestBody MoveDto move) {
        return chessService.makeHumanMove(id, move);
    }

    @PostMapping("/{id}/bot/move")
    @Operation(summary = "make a move")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Move made successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
            @ApiResponse(responseCode = "400", description = "Invalid depth"),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @ResponseStatus(HttpStatus.OK)
    public GameStateDto botMove(@PathVariable String id,
            @RequestParam(defaultValue = "10") int depth) {
        return chessService.makeBotMove(id, depth);
    }
}
