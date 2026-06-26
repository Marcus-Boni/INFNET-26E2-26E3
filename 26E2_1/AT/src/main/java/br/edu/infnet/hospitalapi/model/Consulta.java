package br.edu.infnet.hospitalapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Data da consulta é obrigatória")
    @Column(name = "data_consulta", nullable = false)
    private LocalDateTime dataConsulta;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @NotNull(message = "Paciente é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @NotNull(message = "Médico é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    public Consulta() {
    }

    public Consulta(Long id, LocalDateTime dataConsulta, String observacoes, Paciente paciente, Medico medico) {
        this.id = id;
        this.dataConsulta = dataConsulta;
        this.observacoes = observacoes;
        this.paciente = paciente;
        this.medico = medico;
    }

    public Consulta(LocalDateTime dataConsulta, String observacoes, Paciente paciente, Medico medico) {
        this.dataConsulta = dataConsulta;
        this.observacoes = observacoes;
        this.paciente = paciente;
        this.medico = medico;
    }

    // Getters and Setters
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

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }
}
