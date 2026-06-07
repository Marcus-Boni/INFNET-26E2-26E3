package com.infnet.academia.controller;

import com.infnet.academia.dto.AvaliacaoFisicaRequest;
import com.infnet.academia.model.AvaliacaoFisica;
import com.infnet.academia.service.AvaliacaoFisicaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoFisicaController {

    private final AvaliacaoFisicaService avaliacaoFisicaService;

    public AvaliacaoFisicaController(AvaliacaoFisicaService avaliacaoFisicaService) {
        this.avaliacaoFisicaService = avaliacaoFisicaService;
    }

    @PostMapping
    public ResponseEntity<?> cadastrarAvaliacao(@RequestBody AvaliacaoFisicaRequest request) {
        try {
            AvaliacaoFisica avaliacao = avaliacaoFisicaService.cadastrarAvaliacao(
                    request.getAlunoId(),
                    request.getPeso(),
                    request.getAltura(),
                    request.getPercentualGordura(),
                    request.getAnotacoesMedicas()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(avaliacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<?> listarAvaliacoesDoAluno(@PathVariable Long alunoId) {
        try {
            List<AvaliacaoFisica> avaliacoes = avaliacaoFisicaService.listarAvaliacoesDoAluno(alunoId);
            return ResponseEntity.ok(avaliacoes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
