package com.bill.bill_chess.service.impl;

import com.bill.bill_chess.core.GameConstants;
import com.bill.bill_chess.infra.exception.ChessEngineException;
import com.bill.bill_chess.infra.exception.GameNotFoundException;
import com.bill.bill_chess.infra.exception.IllegalMoveException;
import com.bill.bill_chess.infra.exception.InvalidTurnException;
import com.bill.bill_chess.service.ChessService;
import com.bill.bill_chess.service.MoveEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bill.bill_chess.service.mapper.ChessMapper;
import com.bill.bill_chess.core.ChessUtil;
import com.bill.bill_chess.core.RuleSet;
import com.bill.bill_chess.domain.enums.CastleRight;
import com.bill.bill_chess.domain.enums.Color;
import com.bill.bill_chess.domain.enums.GameStatus;
import com.bill.bill_chess.domain.model.Board;
import com.bill.bill_chess.domain.model.ChessGame;
import com.bill.bill_chess.domain.model.Move;
import com.bill.bill_chess.domain.model.Position;
import com.bill.bill_chess.controller.dto.GameStateDto;
import com.bill.bill_chess.controller.dto.LegalMovesDto;
import com.bill.bill_chess.controller.dto.MoveDto;
import com.bill.bill_chess.persistence.ChessEntity;
import com.bill.bill_chess.persistence.ChessRepository;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChessServiceImpl implements ChessService {

    private final ChessRepository chessRepository;
    private final ChessMapper chessMapper;
    private final MoveEngine localEngine;
    private final ExecutorService stockfishExecutor;

    @Transactional
    @Override
    public GameStateDto createGame() {
        List<ChessEntity> entities = chessRepository.findAllByFenBoard(GameConstants.FEN_INIT);
        if (!entities.isEmpty()) {
            return chessMapper.toGameStateDto(entities.getFirst());
        }
        ChessEntity entity = ChessEntity.initial();
        entity = chessRepository.save(entity);
        return chessMapper.toGameStateDto(entity);
    }

    @Transactional
    private GameStateDto makeMove(String gameId, MoveDto dto) {
        ChessEntity entity = getEntity(gameId);
        ChessGame game = chessMapper.toDomain(entity);
        validateTurn(dto, game.getActiveColor());
        Move m = Move.fromUci(dto.uci());
        Move move = Move.quiet(m.from(), m.to(), game.getBoard().pieceAt(m.from())
                .orElseThrow(() -> new GameNotFoundException("Piece not found at source square")));
        validateLegality(game.getBoard(), move, game.getActiveColor(), game.getCastleRights(), game.getEnPassant());
        game.setBoard(ChessUtil.makeMove(game.getBoard(), move));
        game.setCastleRights(updateCastlingRights(game.getCastleRights(), move));
        game.setEnPassant(updateEnPassant(game.getBoard(), move));
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
    public GameStateDto makeHumanMove(String gameId, MoveDto dto) {
        return makeMove(gameId, dto);
    }

    @Transactional
    @Override
    public GameStateDto makeBotMove(String gameId, int depth) {
        ChessEntity entity = getEntity(gameId);
        ChessGame game = chessMapper.toDomain(entity);
        if (game.getPlayerBotColor() != game.getActiveColor()) {
            throw new InvalidTurnException(GameConstants.NOT_BOT_TURN_MSG);
        }
        String uci = botMove(entity.toFen(), depth <= 0 ? GameConstants.DEFAULT_DEPTH : depth);
        return makeMove(gameId, new MoveDto(game.getPlayerBotColor().fen(), uci));
    }

    @Override
    public GameStateDto findById(String gameId) {
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
    public GameStateDto undoMove(String gameId) {
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

    private void validateTurn(MoveDto dto, Color active) {
        if (!dto.color().equalsIgnoreCase(active.fen()))
            throw new InvalidTurnException(GameConstants.NOT_YOUR_TURN_MSG);
    }

    private void validateLegality(Board board, Move move, Color active,
            Set<CastleRight> rights, Position enPassant) {
        boolean legal = RuleSet.generateLegal(board, active, rights, enPassant)
                .stream()
                .anyMatch(m -> m.equals(move));
        log.debug("Checked legality: {} move={}", legal, move.toUci());
        if (!legal)
            throw new IllegalMoveException("Illegal move: " + move.toUci());
    }

    private Set<CastleRight> updateCastlingRights(Set<CastleRight> current, Move move) {
        if (move.pieceMoved().isKing()) {
            current.remove(move.pieceMoved().isWhite() ? CastleRight.WHITE_KINGSIDE
                    : CastleRight.BLACK_KINGSIDE);
            current.remove(move.pieceMoved().isWhite() ? CastleRight.WHITE_QUEENSIDE
                    : CastleRight.BLACK_QUEENSIDE);
            return current;
        }
        if (move.pieceMoved().isRook()) {
            Position from = move.from();
            int rank = from.rank();
            Color cor = move.pieceMoved().color();
            if (from.file() == 7 && rank == (cor.isWhite() ? 1 : 8)) {
                current.remove(move.pieceMoved().isWhite() ? CastleRight.WHITE_KINGSIDE
                        : CastleRight.BLACK_KINGSIDE);
            }
            if (from.file() == 0 && rank == (cor.isWhite() ? 1 : 8)) {
                current.remove(move.pieceMoved().isWhite() ? CastleRight.WHITE_QUEENSIDE
                        : CastleRight.BLACK_QUEENSIDE);
            }
            return current;
        }
        if (move.captured().isPresent() && move.captured().get().isRook()) {
            Position to = move.to();
            int rank = to.rank();
            Color cor = move.captured().get().color();
            if (to.file() == 7 && rank == (cor.isWhite() ? 1 : 8)) {
                current.remove(move.pieceMoved().isWhite() ? CastleRight.WHITE_KINGSIDE
                        : CastleRight.BLACK_KINGSIDE);
            }
            if (to.file() == 0 && rank == (cor.isWhite() ? 1 : 8)) {
                current.remove(move.pieceMoved().isWhite() ? CastleRight.WHITE_QUEENSIDE
                        : CastleRight.BLACK_QUEENSIDE);
            }
        }
        return current;
    }

    private Position updateEnPassant(Board board, Move move) {
        if (!move.pieceMoved().isPawn())
            return null;
        Position from = move.from();
        Position to = move.to();
        int dr = Math.abs(to.rank() - from.rank());
        if (dr == 2) {
            return Position.of((from.rank() + to.rank()) / 2, from.file());
        }
        return null;
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

}