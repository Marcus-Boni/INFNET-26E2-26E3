package com.techmarket.authservice.exception;

public class TokenRefreshException extends RuntimeException {

    public TokenRefreshException(String token, String message) {
        super(String.format("Falha na renovação do token [%s]: %s", token, message));
    }

    public TokenRefreshException(String message) {
        super(message);
    }
}
