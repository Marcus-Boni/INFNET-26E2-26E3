package com.infnet.tp4.infrastructure.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("Produto não encontrado com id: " + id);
    }
}

