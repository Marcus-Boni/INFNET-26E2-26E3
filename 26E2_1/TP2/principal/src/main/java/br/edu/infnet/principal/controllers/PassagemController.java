package br.edu.infnet.principal.controllers;

import br.edu.infnet.principal.dtos.PassagemRequestDTO;
import br.edu.infnet.principal.dtos.PassagemResponseDTO;
import br.edu.infnet.principal.services.PassagemService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/passagens")
public class PassagemController {

    private final PassagemService passagemService;

    public PassagemController(PassagemService passagemService) {
        this.passagemService = passagemService;
    }

    @GetMapping
    public List<PassagemResponseDTO> getAll() {
        return passagemService.findAll();
    }

    @GetMapping("/{id}")
    public PassagemResponseDTO getById(@PathVariable Long id) {
        return passagemService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PassagemResponseDTO create(@RequestBody PassagemRequestDTO dto) {
        return passagemService.create(dto);
    }

    @PutMapping("/{id}")
    public PassagemResponseDTO update(@PathVariable Long id, @RequestBody PassagemRequestDTO dto) {
        return passagemService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        passagemService.delete(id);
    }

    @GetMapping("/busca")
    public List<PassagemResponseDTO> getByDestino(@RequestParam String destino) {
        return passagemService.findByDestino(destino);
    }
}
