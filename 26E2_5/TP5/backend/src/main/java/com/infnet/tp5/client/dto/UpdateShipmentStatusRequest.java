package com.infnet.tp5.client.dto;

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


