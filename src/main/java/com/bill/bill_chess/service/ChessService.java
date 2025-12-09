package com.bill.bill_chess.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.bill.bill_chess.core.ChessMapper;
import com.bill.bill_chess.core.RuleSet;
import com.bill.bill_chess.domain.enums.CastleRight;
import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.enums.GameStatus;
import com.bill.bill_chess.domain.model.Board;
import com.bill.bill_chess.domain.model.ChessGame;
import com.bill.bill_chess.domain.model.Move;
import com.bill.bill_chess.domain.model.Position;
import com.bill.bill_chess.dto.GameStateDto;
import com.bill.bill_chess.dto.LegalMovesDto;
import com.bill.bill_chess.dto.MoveDto;
import com.bill.bill_chess.persistence.ChessEntity;
import com.bill.bill_chess.persistence.ChessRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChessService {

    private final ChessRepository chessRepository;
    private final ChessMapper chessMapper;
    private final WebClient stockfish = WebClient.create("https://stockfish.online");

    /* ---------- Criar nova partida ---------- */
    @Transactional
    public GameStateDto createGame() {
        ChessEntity entity = ChessEntity.initial();
        entity = chessRepository.save(entity);
        return chessMapper.toGameStateDto(entity);
    }

    /* ---------- Jogada ---------- */
    @Transactional
    public GameStateDto makeMove(String gameId, MoveDto dto) {
        // 1) busca
        ChessEntity entity = chessRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        // 2) converte para objetos de domínio
        ChessGame game = chessMapper.toDomain(entity);

        // 3) validações
        validateTurn(dto, game.getActiveColor());
        Move move = Move.fromUci(dto.uci());
        validateLegality(game.getBoard(), move, game.getActiveColor(), game.getCastleRights(), game.getEnPassant());

        // 4) executa o lance
        game.getBoard().doMove(move);
        game.setCastleRights(updateCastlingRights(game.getCastleRights(), move));
        game.setEnPassant(updateEnPassant(game.getBoard(), move));
        game.setActiveColor(game.getActiveColor().opposite());
        if (move.getPieceMoved().isPawn()) {
            game.setHalfMoveClock(0);
        } else {
            int half = move.captured().isPresent() ? 0 : entity.halfMoveClock() + 1;
            game.setHalfMoveClock(half);
        }
        int full = game.getActiveColor() == Color.WHITE ? entity.fullMoveNumber() + 1 : entity.fullMoveNumber();
        game.setFullMoveNumber(full);
        // 5) status final
        GameStatus status = RuleSet.classify(game.getBoard(), game.getActiveColor(), game.getCastleRights(),
                game.getEnPassant());
        game.setStatus(status);

        // 6) salva
        ChessEntity updated = chessMapper.toEntity(game);
        updated = chessRepository.save(updated);
        return chessMapper.toGameStateDto(updated);
    }

    public GameStateDto makeHumanMove(String gameId, MoveDto dto) {
        return makeMove(gameId, dto);
    }

    /** using when the opponent is the BOT. */
    @Transactional
    public GameStateDto makeBotMove(String gameId, int depth) {
        // 1) current state
        ChessEntity entity = chessRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        ChessGame game = chessMapper.toDomain(entity);

        // 2) only plays if it is the bot's turn
        if (game.getPlayerBotColor() != game.getActiveColor()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not bot turn");
        }

        // 3) asks the engine
        String uci = askEngine(entity.toFen(), depth);

        // 4) reuses the existing flow
        return makeMove(gameId, new MoveDto(game.getPlayerBotColor().name(), uci));
    }

    public LegalMovesDto getLegalMoves(String gameId) {
        ChessEntity entity = chessRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        ChessGame game = chessMapper.toDomain(entity);
        List<String> moves = RuleSet
                .generateLegal(game.getBoard(), game.getActiveColor(), game.getCastleRights(), game.getEnPassant())
                .stream().map(Move::toUci).toList();
        return new LegalMovesDto(moves);
    }

    public GameStateDto getGame(String gameId) {
        ChessEntity entity = chessRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        return chessMapper.toGameStateDto(entity);
    }

    private void validateTurn(MoveDto dto, Color active) {
        if (!dto.color().equalsIgnoreCase(active.fen()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not your turn");
    }

    private void validateLegality(Board board, Move move, Color active,
            Set<CastleRight> rights, Position enPassant) {
        boolean legal = RuleSet.generateLegal(board, active, rights, enPassant)
                .stream()
                .anyMatch(m -> m.equals(move));
        if (!legal)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal move: " + move.toUci());
    }

    /* ---------- Atualiza direitos de roque ---------- */
    private Set<CastleRight> updateCastlingRights(Set<CastleRight> current, Move move) {
        // rei move → perde tudo daquela cor
        if (move.getPieceMoved().isKing()) {
            current.remove(move.getPieceMoved().getColor() == Color.WHITE ? CastleRight.WHITE_KINGSIDE
                    : CastleRight.BLACK_KINGSIDE);
            current.remove(move.getPieceMoved().getColor() == Color.WHITE ? CastleRight.WHITE_QUEENSIDE
                    : CastleRight.BLACK_QUEENSIDE);
            return current;
        }
        // torre move → perde só o lado correspondente
        if (move.getPieceMoved().isRook()) {
            Position from = Position.fromNotation(move.getFrom());
            int rank = from.getRank();
            Color cor = move.getPieceMoved().getColor();
            if (from.getFile() == 7 && rank == (cor == Color.WHITE ? 0 : 7)) {
                current.remove(move.getPieceMoved().getColor() == Color.WHITE ? CastleRight.WHITE_KINGSIDE
                        : CastleRight.BLACK_KINGSIDE);
            }
            if (from.getFile() == 0 && rank == (cor == Color.WHITE ? 0 : 7)) {
                current.remove(move.getPieceMoved().getColor() == Color.WHITE ? CastleRight.WHITE_QUEENSIDE
                        : CastleRight.BLACK_QUEENSIDE);
            }
            return current;
        }
        // captura de torre no canto → também perde o lado
        if (move.captured().isPresent() && move.captured().get().isRook()) {
            Position to = Position.fromNotation(move.getTo());
            int rank = to.getRank();
            Color cor = move.captured().get().getColor();
            if (to.getFile() == 7 && rank == (cor == Color.WHITE ? 0 : 7)) {
                current.remove(move.getPieceMoved().getColor() == Color.WHITE ? CastleRight.WHITE_KINGSIDE
                        : CastleRight.BLACK_KINGSIDE);
            }
            if (to.getFile() == 0 && rank == (cor == Color.WHITE ? 0 : 7)) {
                current.remove(move.getPieceMoved().getColor() == Color.WHITE ? CastleRight.WHITE_QUEENSIDE
                        : CastleRight.BLACK_QUEENSIDE);
            }
        }
        return current;
    }

    /* ---------- En-passant ---------- */
    private Position updateEnPassant(Board board, Move move) {
        // só peão que andou 2 casas gera EP
        if (!move.getPieceMoved().isPawn())
            return null;
        Position from = Position.fromNotation(move.getFrom());
        Position to = Position.fromNotation(move.getTo());
        int dr = Math.abs(to.getRank() - from.getRank());
        if (dr == 2) {
            return Position.of((from.getRank() + to.getRank()) / 2, from.getFile());
        }
        return null;
    }

    /* ---------- consulta motor ---------- */
    private String askEngine(String fen, int depth) {
        return stockfish.get()
                .uri(b -> b.path("/api/stockfish.php")
                        .queryParam("fen", fen)
                        .queryParam("depth", depth)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block(); // ex: "e2e4"
    }

}
