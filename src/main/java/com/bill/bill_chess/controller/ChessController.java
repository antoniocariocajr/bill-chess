package com.bill.bill_chess.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bill.bill_chess.dto.GameStateDto;
import com.bill.bill_chess.dto.LegalMovesDto;
import com.bill.bill_chess.dto.MoveDto;
import com.bill.bill_chess.service.ChessService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/chess")
@RequiredArgsConstructor
public class ChessController {

    private final ChessService chessService;

    @PostMapping("/init")
    public GameStateDto initGame() {
        return chessService.createGame();
    }

    @GetMapping("/{id}/legal-moves")
    public LegalMovesDto LegalMoves(@PathVariable String id,@RequestParam String square) {
        return chessService.getLegalMoves(id,square);
    }

    @GetMapping("/{id}")
    public GameStateDto getGame(@PathVariable String id) {
        return chessService.getGame(id);
    }

    @PostMapping("/{id}/move")
    public GameStateDto move(@PathVariable String id, @RequestBody MoveDto move) {
        return chessService.makeHumanMove(id, move);
    }

    @PostMapping("/{id}/bot/move")
    public GameStateDto botMove(@PathVariable String id,
                            @RequestParam(defaultValue = "10") int depth) {
        return chessService.makeBotMove(id, depth);
    }
}
