package com.bill.bill_chess.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bill.bill_chess.dto.ChessDto;
import com.bill.bill_chess.dto.LegalMovesDto;
import com.bill.bill_chess.dto.MoveDto;
import com.bill.bill_chess.service.ChessService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
    public ChessDto initGame(@RequestBody String entity) {

        return chessService.createGame();
    }

    @GetMapping("id/legalMoves")
    public LegalMovesDto LegalMoves(@RequestParam String id) {
        return chessService.getLegalMoves(id);
    }

    @GetMapping("id")
    public ChessDto getGame(@RequestParam String id) {
        return chessService.getGame(id);
    }

    @PutMapping("move/{id}")
    public ChessDto move(@PathVariable String id, @RequestBody MoveDto move) {

        return chessService.makeMove(id, move);
    }
}
