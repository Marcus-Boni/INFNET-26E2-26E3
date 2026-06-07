package com.infnet.academia.controller;

import com.infnet.academia.dto.TreinoRequest;
import com.infnet.academia.model.Treino;
import com.infnet.academia.service.TreinoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/treinos")
public class TreinoController {

    private final TreinoService treinoService;

    public TreinoController(TreinoService treinoService) {
        this.treinoService = treinoService;
    }

    @PostMapping
    public ResponseEntity<?> cadastrarTreino(@RequestBody TreinoRequest request) {
        try {
            Treino treino = treinoService.cadastrarTreino(
                    request.getNome(),
                    request.getFocoPrincipal(),
                    request.getInstrutorId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(treino);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
