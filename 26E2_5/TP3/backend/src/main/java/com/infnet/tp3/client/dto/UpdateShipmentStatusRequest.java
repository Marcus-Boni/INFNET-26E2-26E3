package com.infnet.tp3.client.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateShipmentStatusRequest {
    private String status;
    private String message;
    private String location;
}
