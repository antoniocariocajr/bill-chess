package com.bill.bill_chess.service.impl;

import com.bill.bill_chess.controller.dto.UserResponseDto;
import com.bill.bill_chess.infra.exception.GameNotFoundException;
import com.bill.bill_chess.persistence.ChessEntity;
import com.bill.bill_chess.persistence.ChessRepository;
import com.bill.bill_chess.persistence.User;
import com.bill.bill_chess.persistence.UserRepository;
import com.bill.bill_chess.service.UserService;
import com.bill.bill_chess.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static com.bill.bill_chess.infra.constants.GameConstants.GAME_NOT_FOUND_MSG;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ChessRepository chessRepository;

    @Override
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toResponse);
    }

    @Override
    public UserResponseDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found!"));
        return UserMapper.toResponse(user);
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found!"));
        return UserMapper.toResponse(user);
    }

    @Override
    public UserResponseDto updateAddGame(String id, String idGame) {
        User user = getUser(id);
        ChessEntity game = getGame(id);
        user.addChessEntity(game);
        user = userRepository.save(user);
        return UserMapper.toResponse(user);
    }

    @Override
    public UserResponseDto updateRemoveGame(String id, String idGame) {
        User user = getUser(id);
        ChessEntity game = getGame(id);
        user.removeChessEntity(game);
        chessRepository.delete(game);
        user = userRepository.save(user);
        return UserMapper.toResponse(user);
    }

    @Override
    public UserResponseDto updateAddRole(String id, User.Role role) {
        User user = getUser(id);
        user.addRole(role);
        user = userRepository.save(user);
        return UserMapper.toResponse(user);
    }

    @Override
    public UserResponseDto updateRemoveRole(String id, User.Role role) {
        User user = getUser(id);
        if (user.getRoles().size() > 1) {
            user.removeRole(role);
        }
        user = userRepository.save(user);
        return UserMapper.toResponse(user);
    }

    @Override
    public void deactivate(String id) {
        User user = getUser(id);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void deleteUser(String id) {
        User user = getUser(id);
        userRepository.delete(user);
    }

    private User getUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found!"));
    }

    private ChessEntity getGame(String id) {
        return chessRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(GAME_NOT_FOUND_MSG));
    }
}
