package com.infnet.academia.dto;

import com.infnet.academia.model.Aluno;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AlunoRankingDTO {
    private Aluno aluno;
    private Long totalConclusoes;

    public AlunoRankingDTO(Aluno aluno, Long totalConclusoes) {
        this.aluno = aluno;
        this.totalConclusoes = totalConclusoes;
    }
}
