package br.edu.infnet.hospitalapi.controller;

import br.edu.infnet.hospitalapi.dto.MedicoRequestDTO;
import br.edu.infnet.hospitalapi.dto.MedicoResponseDTO;
import br.edu.infnet.hospitalapi.service.MedicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    @PostMapping
    public ResponseEntity<MedicoResponseDTO> cadastrarMedico(@Valid @RequestBody MedicoRequestDTO requestDTO) {
        MedicoResponseDTO created = medicoService.cadastrarMedico(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MedicoResponseDTO>> listarMedicos() {
        List<MedicoResponseDTO> medicos = medicoService.listarTodos();
        return ResponseEntity.ok(medicos);
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<MedicoResponseDTO>> obterMedicosOrdenadosPorConsultas() {
        List<MedicoResponseDTO> ranking = medicoService.listarMedicosOrdenadosPorConsultasCount();
        return ResponseEntity.ok(ranking);
    }
}
