package com.bill.bill_chess.service;

import com.bill.bill_chess.controller.dto.LoginRequest;
import com.bill.bill_chess.controller.dto.LoginResponse;
import com.bill.bill_chess.controller.dto.UserCreateDto;
import com.bill.bill_chess.controller.dto.UserResponseDto;
import com.bill.bill_chess.persistence.User;

public interface AuthService {
    LoginResponse authenticate(LoginRequest request);

    UserResponseDto registerUser(UserCreateDto dto);

    LoginResponse encode(User user);

    boolean isLoginCorrect(LoginRequest loginRequest, String password);
}
