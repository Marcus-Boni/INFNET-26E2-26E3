package com.infnet.academia.controller;

import com.infnet.academia.service.CatracaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/catraca")
public class CatracaController {

    private final CatracaService catracaService;

    public CatracaController(CatracaService catracaService) {
        this.catracaService = catracaService;
    }

    @PostMapping("/token")
    public ResponseEntity<?> gerarToken(@RequestParam Long alunoId) {
        try {
            String token = catracaService.gerarTokenAcesso(alunoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "token", token,
                    "expiraEmSegundos", 300,
                    "alunoId", alunoId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/validar")
    public ResponseEntity<?> validarToken(@RequestParam String token) {
        try {
            boolean ativo = catracaService.validarToken(token);
            if (ativo) {
                String alunoId = catracaService.obterAlunoIdDoToken(token);
                return ResponseEntity.ok(Map.of(
                        "ativo", true,
                        "mensagem", "Acesso liberado!",
                        "alunoId", alunoId != null ? Long.parseLong(alunoId) : null
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "ativo", false,
                        "mensagem", "Acesso negado: Token inválido ou expirado."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
