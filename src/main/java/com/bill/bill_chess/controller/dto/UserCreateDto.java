package com.bill.bill_chess.controller.dto;

public record UserCreateDto(
        String email,
        String password,
        String name
) {
}
