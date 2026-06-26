package br.edu.infnet.hospitalapi.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ConsultaRequestDTO {

    @NotNull(message = "Data da consulta é obrigatória")
    private LocalDateTime dataConsulta;

    private String observacoes;

    @NotNull(message = "ID do paciente é obrigatório")
    private Long pacienteId;

    @NotNull(message = "ID do médico é obrigatório")
    private Long medicoId;

    public ConsultaRequestDTO() {
    }

    public ConsultaRequestDTO(LocalDateTime dataConsulta, String observacoes, Long pacienteId, Long medicoId) {
        this.dataConsulta = dataConsulta;
        this.observacoes = observacoes;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
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

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public void setMedicoId(Long medicoId) {
        this.medicoId = medicoId;
    }
}
