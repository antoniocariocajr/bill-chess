package com.bill.bill_chess.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bill.bill_chess.controller.dto.LoginRequest;
import com.bill.bill_chess.controller.dto.LoginResponse;
import com.bill.bill_chess.controller.dto.UserCreateDto;
import com.bill.bill_chess.controller.dto.UserResponseDto;
import com.bill.bill_chess.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Authentication", description = "Authentication user of the Chess API")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto register(@RequestBody UserCreateDto user) {
        return authService.registerUser(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.authenticate(request);
    }

}
