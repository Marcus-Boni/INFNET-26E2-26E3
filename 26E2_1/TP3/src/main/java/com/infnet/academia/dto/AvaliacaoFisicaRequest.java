package com.infnet.academia.dto;

import lombok.Data;

@Data
public class AvaliacaoFisicaRequest {
    private Long alunoId;
    private Double peso;
    private Double altura;
    private Double percentualGordura;
    private String anotacoesMedicas;
}
