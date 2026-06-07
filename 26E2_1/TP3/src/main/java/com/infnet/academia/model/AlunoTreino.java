package com.infnet.academia.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "alunos_treinos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlunoTreino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "treino_id", nullable = false)
    private Treino treino;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "status_conclusao", nullable = false)
    private String statusConclusao; // e.g. "CONCLUIDO", "EM_ANDAMENTO"
}
