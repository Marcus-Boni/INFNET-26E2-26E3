package br.edu.infnet.hospitalapi.service;

import br.edu.infnet.hospitalapi.dto.PacienteRequestDTO;
import br.edu.infnet.hospitalapi.dto.PacienteResponseDTO;
import br.edu.infnet.hospitalapi.exception.ResourceNotFoundException;
import br.edu.infnet.hospitalapi.model.Paciente;
import br.edu.infnet.hospitalapi.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Transactional
    public PacienteResponseDTO cadastrarPaciente(PacienteRequestDTO dto) {
        Paciente paciente = new Paciente(
                dto.getNome(),
                dto.getCpf(),
                dto.getDataNascimento(),
                dto.getTelefone()
        );
        Paciente saved = pacienteRepository.save(paciente);
        return convertToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public PacienteResponseDTO buscarPorId(Long id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente com ID " + id + " não encontrado."));
        return convertToResponseDTO(paciente);
    }

    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> listarTodos() {
        return pacienteRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removerPaciente(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paciente com ID " + id + " não encontrado para exclusão.");
        }
        pacienteRepository.deleteById(id);
    }

    private PacienteResponseDTO convertToResponseDTO(Paciente paciente) {
        return new PacienteResponseDTO(
                paciente.getId(),
                paciente.getNome(),
                paciente.getCpf(),
                paciente.getDataNascimento(),
                paciente.getTelefone()
        );
    }
}
