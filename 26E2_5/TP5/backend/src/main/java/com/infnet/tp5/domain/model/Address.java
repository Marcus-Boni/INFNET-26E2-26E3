package com.infnet.tp5.domain.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
}


