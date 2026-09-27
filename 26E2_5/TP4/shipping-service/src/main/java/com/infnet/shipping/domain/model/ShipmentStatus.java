package com.infnet.shipping.domain.model;

public enum ShipmentStatus {
    CREATED("Criado"),
    DISPATCHED("Despachado"),
    IN_TRANSIT("Em Trânsito"),
    OUT_FOR_DELIVERY("Saiu para Entrega"),
    DELIVERED("Entregue"),
    CANCELLED("Cancelado");

    private final String displayName;

    ShipmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
