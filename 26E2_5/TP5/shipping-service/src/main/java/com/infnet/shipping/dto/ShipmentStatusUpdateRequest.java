package com.infnet.shipping.dto;

import com.infnet.shipping.domain.model.ShipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentStatusUpdateRequest {

    @NotNull(message = "O novo status é obrigatório.")
    private ShipmentStatus status;

    @NotBlank(message = "A mensagem do evento de rastreio é obrigatória.")
    private String message;

    @NotBlank(message = "A localização atual é obrigatória.")
    private String location;
}
