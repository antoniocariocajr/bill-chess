package com.bill.bill_chess.dto;

import java.util.List;

public record UserResponseDto(
        String id,
        String email,
        String name,
        List<String> idGame,
        boolean active
) {
}
