package com.infnet.academia.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "instrutores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instrutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String cref;

    @OneToMany(mappedBy = "instrutor")
    @JsonIgnore
    @ToString.Exclude
    private List<Treino> treinos;
}
