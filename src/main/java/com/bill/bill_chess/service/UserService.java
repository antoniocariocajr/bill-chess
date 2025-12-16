package com.bill.bill_chess.service;

import com.bill.bill_chess.controller.dto.UserResponseDto;
import com.bill.bill_chess.persistence.User;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.data.domain.Page;

public interface UserService {

    Page<UserResponseDto> getAllUsers(Pageable pageable);

    UserResponseDto getUserById(String id, JwtAuthenticationToken token);

    UserResponseDto getUserByEmail(String email, JwtAuthenticationToken token);

    UserResponseDto updateAddGame(String id, String idGame, JwtAuthenticationToken token);

    UserResponseDto updateRemoveGame(String id, String idGame, JwtAuthenticationToken token);

    UserResponseDto updateAddRole(String id, User.Role role);

    UserResponseDto updateRemoveRole(String id, User.Role role);

    void deactivate(String id);

    void deleteUser(String id);
}
