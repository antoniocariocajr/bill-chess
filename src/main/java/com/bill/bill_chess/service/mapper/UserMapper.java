package com.bill.bill_chess.service.mapper;

import com.bill.bill_chess.controller.dto.UserCreateDto;
import com.bill.bill_chess.controller.dto.UserResponseDto;
import com.bill.bill_chess.persistence.User;

import java.util.EnumSet;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public final class UserMapper {

    public static User toEntity(UserCreateDto dto, String passwordEncoder) {
        return User.builder()
                .email(dto.email())
                .password(passwordEncoder)
                .name(dto.name())
                .roles(EnumSet.of(User.Role.PLAYER))
                .build();
    }

    public static UserResponseDto toResponse(User user) {
        Map<String, String> mapGame = user.getChessEntities()
                .entrySet()
                .stream()
                .collect(toMap(k -> k.getKey().id(), v -> v.getValue().fen()));
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                mapGame,
                user.isEnabled());
    }
}
