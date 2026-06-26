package br.edu.infnet.hospitalapi.controller;

import br.edu.infnet.hospitalapi.dto.PacienteRequestDTO;
import br.edu.infnet.hospitalapi.dto.PacienteResponseDTO;
import br.edu.infnet.hospitalapi.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping
    public ResponseEntity<PacienteResponseDTO> cadastrarPaciente(@Valid @RequestBody PacienteRequestDTO requestDTO) {
        PacienteResponseDTO created = pacienteService.cadastrarPaciente(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> buscarPacientePorId(@PathVariable Long id) {
        PacienteResponseDTO paciente = pacienteService.buscarPorId(id);
        return ResponseEntity.ok(paciente);
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponseDTO>> listarPacientes() {
        List<PacienteResponseDTO> pacientes = pacienteService.listarTodos();
        return ResponseEntity.ok(pacientes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerPaciente(@PathVariable Long id) {
        pacienteService.removerPaciente(id);
        return ResponseEntity.noContent().build();
    }
}
