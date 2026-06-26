package br.edu.infnet.hospitalapi.dto;

import java.time.LocalDateTime;

public class ConsultaResponseDTO {

    private Long id;
    private LocalDateTime dataConsulta;
    private String observacoes;
    private PacienteResponseDTO paciente;
    private MedicoResponseDTO medico;

    public ConsultaResponseDTO() {
    }

    public ConsultaResponseDTO(Long id, LocalDateTime dataConsulta, String observacoes, PacienteResponseDTO paciente, MedicoResponseDTO medico) {
        this.id = id;
        this.dataConsulta = dataConsulta;
        this.observacoes = observacoes;
        this.paciente = paciente;
        this.medico = medico;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataConsulta() {
        return dataConsulta;
    }

    public void setDataConsulta(LocalDateTime dataConsulta) {
        this.dataConsulta = dataConsulta;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public PacienteResponseDTO getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteResponseDTO paciente) {
        this.paciente = paciente;
    }

    public MedicoResponseDTO getMedico() {
        return medico;
    }

    public void setMedico(MedicoResponseDTO medico) {
        this.medico = medico;
    }
}
