package com.bill.bill_chess.dto;

public record UserCreateDto(
        String email,
        String password,
        String name
) {
}
