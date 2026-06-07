package com.infnet.academia.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "treinos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Treino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "foco_principal", nullable = false)
    private String focoPrincipal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "instrutor_id", nullable = false)
    private Instrutor instrutor;
}
