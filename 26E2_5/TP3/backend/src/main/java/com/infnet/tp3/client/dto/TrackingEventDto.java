package com.infnet.tp3.client.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingEventDto {
    private Long id;
    private String status;
    private String statusDisplay;
    private String message;
    private String location;
    private LocalDateTime timestamp;
}
