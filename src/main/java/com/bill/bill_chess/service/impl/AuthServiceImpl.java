package com.bill.bill_chess.service.impl;

import com.bill.bill_chess.controller.dto.LoginRequest;
import com.bill.bill_chess.controller.dto.LoginResponse;
import com.bill.bill_chess.controller.dto.UserCreateDto;
import com.bill.bill_chess.controller.dto.UserResponseDto;
import com.bill.bill_chess.persistence.User;
import com.bill.bill_chess.persistence.UserRepository;
import com.bill.bill_chess.service.AuthService;
import com.bill.bill_chess.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.bill.bill_chess.core.GameConstants.ISSUER;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    @Override
    public UserResponseDto registerUser(UserCreateDto dto) {
        Optional<User> userOptional = userRepository.findByEmail(dto.email());
        if (userOptional.isPresent()) throw new ResponseStatusException(CONFLICT,"Usuario ja cadastrado!");
        User newUser = UserMapper.toEntity(dto, passwordEncoder.encode(dto.password()));
        newUser = userRepository.save(newUser);
        return UserMapper.toResponse(newUser);
    }

    @Override
    public LoginResponse authenticate(LoginRequest loginRequest) {
        System.out.println("login: "+ loginRequest.email());
        var user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        if (isLoginCorrect(loginRequest, user.getPassword())) {
            System.out.println("verificou a senha");
            return encode(user);
        }
        throw new ResponseStatusException(UNAUTHORIZED,"user or password is invalid!");
    }

    @Override
    public LoginResponse encode(User user) {
        var now = Instant.now();
        var expiresIn = 60000L;
        var scopes = user.getRoles()
                .stream()
                .map(Enum::name)
                .collect(Collectors.joining(" "));
        var claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(user.getId())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("scope", scopes)
                .build();
        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new LoginResponse(jwtValue);
    }

    @Override
    public boolean isLoginCorrect(LoginRequest loginRequest, String password) {
        return passwordEncoder.matches(loginRequest.password(), password);
    }
}
