package com.infnet.shipping.dto;

import com.infnet.shipping.domain.model.ShipmentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingEventDto {
    private Long id;
    private ShipmentStatus status;
    private String statusDisplay;
    private String message;
    private String location;
    private LocalDateTime timestamp;
}
