package br.edu.infnet.hospitalapi.service;

import br.edu.infnet.hospitalapi.dto.ConsultaRequestDTO;
import br.edu.infnet.hospitalapi.dto.ConsultaResponseDTO;
import br.edu.infnet.hospitalapi.dto.MedicoResponseDTO;
import br.edu.infnet.hospitalapi.dto.PacienteResponseDTO;
import br.edu.infnet.hospitalapi.exception.ResourceNotFoundException;
import br.edu.infnet.hospitalapi.model.Consulta;
import br.edu.infnet.hospitalapi.model.Medico;
import br.edu.infnet.hospitalapi.model.Paciente;
import br.edu.infnet.hospitalapi.repository.ConsultaRepository;
import br.edu.infnet.hospitalapi.repository.MedicoRepository;
import br.edu.infnet.hospitalapi.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;

    public ConsultaService(ConsultaRepository consultaRepository,
                           PacienteRepository pacienteRepository,
                           MedicoRepository medicoRepository) {
        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
    }

    @Transactional
    public ConsultaResponseDTO cadastrarConsulta(ConsultaRequestDTO dto) {
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente com ID " + dto.getPacienteId() + " não encontrado."));

        Medico medico = medicoRepository.findById(dto.getMedicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Médico com ID " + dto.getMedicoId() + " não encontrado."));

        Consulta consulta = new Consulta(
                dto.getDataConsulta(),
                dto.getObservacoes(),
                paciente,
                medico
        );

        Consulta saved = consultaRepository.save(consulta);
        return convertToResponseDTO(saved);
    }

    private ConsultaResponseDTO convertToResponseDTO(Consulta consulta) {
        PacienteResponseDTO pacienteDTO = new PacienteResponseDTO(
                consulta.getPaciente().getId(),
                consulta.getPaciente().getNome(),
                consulta.getPaciente().getCpf(),
                consulta.getPaciente().getDataNascimento(),
                consulta.getPaciente().getTelefone()
        );

        MedicoResponseDTO medicoDTO = new MedicoResponseDTO(
                consulta.getMedico().getId(),
                consulta.getMedico().getNome(),
                consulta.getMedico().getCrm(),
                consulta.getMedico().getEspecialidade()
        );

        return new ConsultaResponseDTO(
                consulta.getId(),
                consulta.getDataConsulta(),
                consulta.getObservacoes(),
                pacienteDTO,
                medicoDTO
        );
    }
}
