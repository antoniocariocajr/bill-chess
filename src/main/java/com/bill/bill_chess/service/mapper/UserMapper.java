package com.bill.bill_chess.service.mapper;

import com.bill.bill_chess.controller.dto.UserCreateDto;
import com.bill.bill_chess.controller.dto.UserResponseDto;
import com.bill.bill_chess.persistence.User;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

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
        Map<String, String> mapGAme = new HashMap<>();
        user.getChessEntities().forEach((key, value) -> mapGAme.put(key.id(), value.fen()));
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                mapGAme,
                user.isEnabled());
    }
}
