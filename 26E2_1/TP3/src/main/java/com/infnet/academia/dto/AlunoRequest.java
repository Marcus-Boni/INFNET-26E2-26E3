package com.infnet.academia.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AlunoRequest {
    private String nome;
    private String email;
    private LocalDate dataNascimento;
    private boolean ativo;
    private Long planoId;
}
