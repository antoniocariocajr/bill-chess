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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/chess")
@RequiredArgsConstructor
public class ChessController {

    private final ChessService chessService;

    @PostMapping("path")
    public GameStateDto initGame(@RequestBody String entity) {

        return chessService.createGame();
    }

    @GetMapping("/{id}/legalMoves")
    public LegalMovesDto LegalMoves(@RequestParam String id) {
        return chessService.getLegalMoves(id);
    }

    @GetMapping("/{id}")
    public GameStateDto getGame(@RequestParam String id) {
        return chessService.getGame(id);
    }

    @PutMapping("/{id}/move")
    public GameStateDto move(@PathVariable String id, @RequestBody MoveDto move) {

        return chessService.makeMove(id, move);
    }

    /* ---------- bot (opcional) ---------- */
    @PostMapping("/{id}/bot/move")
    public ResponseEntity<GameStateDto> botMove(@PathVariable String id,
            @RequestParam(defaultValue = "10") int depth) {
        return ResponseEntity.ok(chessService.makeBotMove(id, depth));
    }
}
