package com.infnet.academia.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document(collection = "avaliacoes_fisicas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvaliacaoFisica {

    @Id
    private String id;

    private Long alunoId;
    private Double peso;
    private Double altura;
    private Double percentualGordura;
    private String anotacoesMedicas;
}
