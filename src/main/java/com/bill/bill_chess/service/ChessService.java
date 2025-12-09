package com.bill.bill_chess.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import com.bill.bill_chess.dto.ChessDto;
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

    /* ---------- Criar nova partida ---------- */
    @Transactional
    public ChessDto createGame() {
        ChessEntity entity = ChessEntity.initial();
        entity = chessRepository.save(entity);
        return chessMapper.toDto(entity);
    }

    /* ---------- Jogada ---------- */
    @Transactional
    public ChessDto makeMove(String gameId, MoveDto dto) {
        // 1) busca
        ChessEntity entity = chessRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        // 2) converte para objetos de domínio
        ChessGame game = chessMapper.toDomain(entity);

        // 3) validações
        validateTurn(dto, game.getCorrentColor());
        Move move = Move.fromUci(dto.uci());
        validateLegality(game.getBoard(), move, game.getCorrentColor(), game.getCastleRights(), game.getEnPassant());

        // 4) executa o lance
        game.getBoard().doMove(move);
        game.setCastleRights(updateCastlingRights(game.getCastleRights(), move));
        game.setEnPassant(updateEnPassant(game.getBoard(), move));
        game.setCorrentColor(game.getCorrentColor().opposite());
        if (move.getPieceMoved().isPawn()) {
            game.setHalfMoveClock(0);
        } else {
            int half = move.captured().isPresent() ? 0 : entity.halfMoveClock() + 1;
            game.setHalfMoveClock(half);
        }
        int full = game.getCorrentColor() == Color.WHITE ? entity.fullMoveNumber() + 1 : entity.fullMoveNumber();
        game.setFullMoveNumber(full);
        // 5) status final
        GameStatus status = RuleSet.classify(game.getBoard(), game.getCorrentColor(), game.getCastleRights(),
                game.getEnPassant());
        game.setStatus(status);

        // 6) salva
        ChessEntity updated = chessMapper.toEntity(game);
        updated = chessRepository.save(updated);
        return chessMapper.toDto(updated);
    }

    public LegalMovesDto getLegalMoves(String gameId) {
        ChessEntity entity = chessRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        ChessGame game = chessMapper.toDomain(entity);
        List<String> moves = RuleSet
                .generateLegal(game.getBoard(), game.getCorrentColor(), game.getCastleRights(), game.getEnPassant())
                .stream().map(Move::toUci).toList();
        return new LegalMovesDto(moves);
    }

    public ChessDto getGame(String gameId) {
        ChessEntity entity = chessRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        return chessMapper.toDto(entity);
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

}
