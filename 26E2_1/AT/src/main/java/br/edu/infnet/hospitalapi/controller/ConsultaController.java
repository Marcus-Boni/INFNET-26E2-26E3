package br.edu.infnet.hospitalapi.controller;

import br.edu.infnet.hospitalapi.dto.ConsultaRequestDTO;
import br.edu.infnet.hospitalapi.dto.ConsultaResponseDTO;
import br.edu.infnet.hospitalapi.service.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @PostMapping
    public ResponseEntity<ConsultaResponseDTO> cadastrarConsulta(@Valid @RequestBody ConsultaRequestDTO requestDTO) {
        ConsultaResponseDTO created = consultaService.cadastrarConsulta(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
