package com.bill.bill_chess.service.validation;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class UserValidation {
    private UserValidation() {
    }

    public static boolean isTokenAdmin(JwtAuthenticationToken token) {
        return token.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SCOPE_ADMIN"));
    }

    public static void validateUser(JwtAuthenticationToken token, String idUser) {
        if (isTokenAdmin(token))
            return;
        if (!token.getName().equals(idUser))
            throw new IllegalArgumentException("User not authorized");
    }
}
