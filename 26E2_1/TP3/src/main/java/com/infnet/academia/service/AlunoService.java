package com.infnet.academia.service;

import com.infnet.academia.dto.AlunoRankingDTO;
import com.infnet.academia.model.Aluno;
import com.infnet.academia.model.Plano;
import com.infnet.academia.repository.AlunoRepository;
import com.infnet.academia.repository.AlunoTreinoRepository;
import com.infnet.academia.repository.PlanoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final PlanoRepository planoRepository;
    private final AlunoTreinoRepository alunoTreinoRepository;

    public AlunoService(AlunoRepository alunoRepository,
                        PlanoRepository planoRepository,
                        AlunoTreinoRepository alunoTreinoRepository) {
        this.alunoRepository = alunoRepository;
        this.planoRepository = planoRepository;
        this.alunoTreinoRepository = alunoTreinoRepository;
    }

    @Transactional
    public Aluno cadastrarAluno(String nome, String email, LocalDate dataNascimento, boolean ativo, Long planoId) {
        Plano plano = planoRepository.findById(planoId)
                .orElseThrow(() -> new IllegalArgumentException("Plano com ID " + planoId + " não encontrado."));

        Aluno aluno = Aluno.builder()
                .nome(nome)
                .email(email)
                .dataNascimento(dataNascimento)
                .ativo(ativo)
                .plano(plano)
                .build();

        return alunoRepository.save(aluno);
    }

    public List<Aluno> listarAlunosAtivos() {
        return alunoRepository.findByAtivoTrue();
    }

    public List<AlunoRankingDTO> obterRankingAlunos() {
        return alunoTreinoRepository.obterRankingAlunos();
    }
}
