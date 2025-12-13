package com.bill.bill_chess.service.mapper;

import com.bill.bill_chess.controller.dto.UserCreateDto;
import com.bill.bill_chess.controller.dto.UserResponseDto;
import com.bill.bill_chess.persistence.ChessEntity;
import com.bill.bill_chess.persistence.User;

import java.util.EnumSet;

public final class UserMapper {

    public static User toEntity(UserCreateDto dto, String passwordEncoder){
        return User.builder()
                .email(dto.email())
                .password(passwordEncoder)
                .name(dto.name())
                .roles(EnumSet.of(User.Role.PLAYER))
                .build();
    }
    public static UserResponseDto toResponse(User user){
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getChessEntities().stream().map(ChessEntity::id).toList(),
                user.isEnabled()
        );
    }
}
