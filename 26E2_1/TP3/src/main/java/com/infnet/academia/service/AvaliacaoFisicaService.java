package com.infnet.academia.service;

import com.infnet.academia.model.AvaliacaoFisica;
import com.infnet.academia.repository.AlunoRepository;
import com.infnet.academia.repository.AvaliacaoFisicaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvaliacaoFisicaService {

    private final AvaliacaoFisicaRepository avaliacaoFisicaRepository;
    private final AlunoRepository alunoRepository;

    public AvaliacaoFisicaService(AvaliacaoFisicaRepository avaliacaoFisicaRepository,
                                  AlunoRepository alunoRepository) {
        this.avaliacaoFisicaRepository = avaliacaoFisicaRepository;
        this.alunoRepository = alunoRepository;
    }

    public AvaliacaoFisica cadastrarAvaliacao(Long alunoId, Double peso, Double altura, Double percentualGordura, String anotacoesMedicas) {
        // Validação poliglota: Verifica se o aluno existe no banco relacional (JPA)
        if (!alunoRepository.existsById(alunoId)) {
            throw new IllegalArgumentException("Aluno com ID " + alunoId + " não encontrado no banco relacional.");
        }

        AvaliacaoFisica avaliacao = AvaliacaoFisica.builder()
                .alunoId(alunoId)
                .peso(peso)
                .altura(altura)
                .percentualGordura(percentualGordura)
                .anotacoesMedicas(anotacoesMedicas)
                .build();

        return avaliacaoFisicaRepository.save(avaliacao);
    }

    public List<AvaliacaoFisica> listarAvaliacoesDoAluno(Long alunoId) {
        // Opcional: valida se o aluno existe
        if (!alunoRepository.existsById(alunoId)) {
            throw new IllegalArgumentException("Aluno com ID " + alunoId + " não encontrado.");
        }
        return avaliacaoFisicaRepository.findByAlunoId(alunoId);
    }
}
