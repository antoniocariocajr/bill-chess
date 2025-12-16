package com.bill.bill_chess.controller.dto;

import java.util.Map;

public record UserResponseDto(
                String id,
                String email,
                String name,
                Map<String, String> mapGame,
                boolean active) {
}
