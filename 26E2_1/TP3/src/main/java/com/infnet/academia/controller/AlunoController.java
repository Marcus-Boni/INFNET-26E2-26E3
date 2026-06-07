package com.infnet.academia.controller;

import com.infnet.academia.dto.AlunoRankingDTO;
import com.infnet.academia.dto.AlunoRequest;
import com.infnet.academia.model.Aluno;
import com.infnet.academia.service.AlunoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @PostMapping
    public ResponseEntity<?> cadastrarAluno(@RequestBody AlunoRequest request) {
        try {
            Aluno aluno = alunoService.cadastrarAluno(
                    request.getNome(),
                    request.getEmail(),
                    request.getDataNascimento(),
                    request.isAtivo(),
                    request.getPlanoId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(aluno);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<Aluno>> listarAlunosAtivos() {
        List<Aluno> ativos = alunoService.listarAlunosAtivos();
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<AlunoRankingDTO>> obterRankingAlunos() {
        List<AlunoRankingDTO> ranking = alunoService.obterRankingAlunos();
        return ResponseEntity.ok(ranking);
    }
}
