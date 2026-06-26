package br.edu.infnet.hospitalapi.service;

import br.edu.infnet.hospitalapi.dto.MedicoRequestDTO;
import br.edu.infnet.hospitalapi.dto.MedicoResponseDTO;
import br.edu.infnet.hospitalapi.model.Medico;
import br.edu.infnet.hospitalapi.repository.MedicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;

    public MedicoService(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    @Transactional
    public MedicoResponseDTO cadastrarMedico(MedicoRequestDTO dto) {
        Medico medico = new Medico(
                dto.getNome(),
                dto.getCrm(),
                dto.getEspecialidade()
        );
        Medico saved = medicoRepository.save(medico);
        return convertToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<MedicoResponseDTO> listarTodos() {
        return medicoRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicoResponseDTO> listarMedicosOrdenadosPorConsultasCount() {
        return medicoRepository.findMedicosOrderedByConsultasCount().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private MedicoResponseDTO convertToResponseDTO(Medico medico) {
        return new MedicoResponseDTO(
                medico.getId(),
                medico.getNome(),
                medico.getCrm(),
                medico.getEspecialidade()
        );
    }
}
