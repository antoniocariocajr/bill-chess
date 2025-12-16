package com.bill.bill_chess.service.impl;

import com.bill.bill_chess.infra.constants.GameConstants;
import com.bill.bill_chess.infra.exception.ChessEngineException;
import com.bill.bill_chess.infra.exception.GameNotFoundException;
import com.bill.bill_chess.infra.exception.InvalidTurnException;
import com.bill.bill_chess.service.ChessService;
import com.bill.bill_chess.service.MoveEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bill.bill_chess.service.mapper.ChessMapper;
import com.bill.bill_chess.service.rule.RuleSet;
import com.bill.bill_chess.service.util.ChessUtil;
import com.bill.bill_chess.service.validation.ChessValidation;
import com.bill.bill_chess.domain.enums.CastleRight;
import com.bill.bill_chess.domain.enums.GameStatus;
import com.bill.bill_chess.domain.model.ChessGame;
import com.bill.bill_chess.domain.model.Move;
import com.bill.bill_chess.domain.model.Position;
import com.bill.bill_chess.controller.dto.GameStateDto;
import com.bill.bill_chess.controller.dto.LegalMovesDto;
import com.bill.bill_chess.controller.dto.MoveDto;
import com.bill.bill_chess.persistence.ChessEntity;
import com.bill.bill_chess.persistence.ChessRepository;
import com.bill.bill_chess.persistence.User;
import com.bill.bill_chess.persistence.UserRepository;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChessServiceImpl implements ChessService {

    private final ChessRepository chessRepository;
    private final UserRepository userRepository;
    private final ChessMapper chessMapper;
    private final MoveEngine localEngine;
    private final ExecutorService stockfishExecutor;

    @Transactional
    @Override
    public GameStateDto createGame(JwtAuthenticationToken token) {
        User user = userRepository.findById(token.getName())
                .orElseThrow(() -> new GameNotFoundException("User not found"));
        List<ChessEntity> entities = user.getChessEntities()
                .stream()
                .filter(e -> e.fenBoard().equals(GameConstants.FEN_INIT))
                .toList();
        if (!entities.isEmpty()) {
            return chessMapper.toGameStateDto(entities.getFirst());
        }
        ChessEntity entity = ChessEntity.initial();
        entity = chessRepository.save(entity);
        user.getChessEntities().add(entity);
        userRepository.save(user);
        return chessMapper.toGameStateDto(entity);
    }

    @Transactional
    private GameStateDto makeMove(String gameId, MoveDto dto, JwtAuthenticationToken token) {
        checkUserAuthorization(token, gameId);
        ChessEntity entity = getEntity(gameId);
        ChessGame game = chessMapper.toDomain(entity);
        ChessValidation.validateTurn(dto, game.getActiveColor());
        Move m = Move.fromUci(dto.uci());
        Move move = Move.quiet(m.from(), m.to(), game.getBoard().pieceAt(m.from())
                .orElseThrow(() -> new GameNotFoundException("Piece not found at source square")));
        ChessValidation.validateLegality(game.getBoard(), move, game.getActiveColor(), game.getCastleRights(),
                game.getEnPassant());
        Set<CastleRight> castleRights = ChessUtil.updateCastlingRights(game.getCastleRights(), move);
        game.setBoard(ChessUtil.makeMove(game.getBoard(), move));
        game.setCastleRights(castleRights);
        game.setEnPassant(ChessUtil.updateEnPassant(game.getBoard(), move));
        game.setActiveColor(game.getActiveColor().opposite());
        if (move.pieceMoved().isPawn()) {
            game.setHalfMoveClock(0);
        } else {
            int half = move.captured().isPresent() ? 0 : entity.halfMoveClock() + 1;
            game.setHalfMoveClock(half);
        }
        int full = game.getActiveColor().isWhite() ? entity.fullMoveNumber() + 1 : entity.fullMoveNumber();
        game.setFullMoveNumber(full);
        GameStatus status = RuleSet.classify(game.getBoard(), game.getActiveColor(), game.getCastleRights(),
                game.getEnPassant());
        game.setStatus(status);
        ChessEntity updated = chessMapper.toEntity(game);
        updated = chessRepository.save(updated);
        return chessMapper.toGameStateDto(updated);
    }

    @Override
    public GameStateDto makeHumanMove(String gameId, MoveDto dto, JwtAuthenticationToken token) {
        return makeMove(gameId, dto, token);
    }

    @Transactional
    @Override
    public GameStateDto makeBotMove(String gameId, int depth, JwtAuthenticationToken token) {
        ChessEntity entity = getEntity(gameId);
        ChessGame game = chessMapper.toDomain(entity);
        if (game.getPlayerBotColor() != game.getActiveColor()) {
            throw new InvalidTurnException(GameConstants.NOT_BOT_TURN_MSG);
        }
        String uci = botMove(entity.toFen(), depth <= 0 ? GameConstants.DEFAULT_DEPTH : depth);
        return makeMove(gameId, new MoveDto(game.getPlayerBotColor().fen(), uci), token);
    }

    @Override
    public GameStateDto findById(String gameId, JwtAuthenticationToken token) {
        checkUserAuthorization(token, gameId);
        ChessEntity entity = getEntity(gameId);
        return chessMapper.toGameStateDto(entity);
    }

    @Override
    public LegalMovesDto getLegalMoves(String gameId, String square) {
        Position position = Position.fromNotation(square);
        ChessEntity entity = getEntity(gameId);
        ChessGame game = chessMapper.toDomain(entity);
        List<String> moves = RuleSet
                .generateLegalInPosition(game.getBoard(), game.getActiveColor(), game.getCastleRights(),
                        game.getEnPassant(), position)
                .stream()
                .map(Move::toUci).toList();
        return new LegalMovesDto(moves);
    }

    @Override
    public String getBestMove(String gameId, int depth) {
        ChessEntity entity = getEntity(gameId);
        return botMove(entity.toFen(), depth <= 0 ? GameConstants.DEFAULT_DEPTH : depth);
    }

    @Override
    @Transactional
    public GameStateDto undoMove(String gameId, JwtAuthenticationToken token) {
        checkUserAuthorization(token, gameId);
        ChessEntity entity = getEntity(gameId);
        ChessGame game = chessMapper.toDomain(entity);
        if (game.getActiveColor() != game.getPlayerBotColor()) {
            throw new InvalidTurnException(GameConstants.NOT_BOT_TURN_MSG);
        }
        game.getBoard().undoMove();
        game.getBoard().undoMove();
        entity = chessMapper.toEntity(game);
        entity = chessRepository.save(entity);
        return chessMapper.toGameStateDto(entity);
    }

    private ChessEntity getEntity(String gameId) {
        return chessRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(GameConstants.GAME_NOT_FOUND_MSG));
    }

    private String botMove(String fen, int depth) {
        try {
            return stockfishExecutor
                    .submit(() -> localEngine.bestMove(fen, depth)
                            .timeout(Duration.ofSeconds(GameConstants.BOT_TIMEOUT_SECONDS))
                            .doOnError(err -> log.error("Stockfish failed locally", err))
                            .block()) // timeout já configurado no Mono
                    .get(GameConstants.BOT_TIMEOUT_SECONDS, TimeUnit.SECONDS); // timeout total (executor + block)
        } catch (TimeoutException tex) {
            throw new ChessEngineException("Time limit exceeded for calculation", tex);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new ChessEngineException("Interrupted", ie);
        } catch (ExecutionException e) {
            Throwable root = e.getCause();
            log.error("Stockfish execution error", root);
            throw new ChessEngineException(root.getMessage(), root);
        }
    }

    private void checkUserAuthorization(JwtAuthenticationToken token, String gameId) {
        if (!userRepository.existsByIdAndChessEntityId(token.getName(), gameId)) {
            throw new IllegalArgumentException("User not authorized");
        }
    }
}