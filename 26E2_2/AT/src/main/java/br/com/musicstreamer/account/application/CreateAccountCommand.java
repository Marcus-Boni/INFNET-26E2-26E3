package br.com.musicstreamer.account.application;

public record CreateAccountCommand(
    String name,
    String email,
    String cardNumber,
    String cardLimit,
    boolean cardActive
) {}
