package br.edu.infnet.hospitalapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "internacoes")
public class Internacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Data de entrada é obrigatória")
    @Column(name = "data_entrada", nullable = false)
    private LocalDateTime dataEntrada;

    @Column(name = "data_alta")
    private LocalDateTime dataAlta;

    @NotBlank(message = "Quarto é obrigatório")
    @Column(nullable = false)
    private String quarto;

    @NotNull(message = "Paciente é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    public Internacao() {
    }

    public Internacao(Long id, LocalDateTime dataEntrada, LocalDateTime dataAlta, String quarto, Paciente paciente) {
        this.id = id;
        this.dataEntrada = dataEntrada;
        this.dataAlta = dataAlta;
        this.quarto = quarto;
        this.paciente = paciente;
    }

    public Internacao(LocalDateTime dataEntrada, LocalDateTime dataAlta, String quarto, Paciente paciente) {
        this.dataEntrada = dataEntrada;
        this.dataAlta = dataAlta;
        this.quarto = quarto;
        this.paciente = paciente;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDateTime dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public LocalDateTime getDataAlta() {
        return dataAlta;
    }

    public void setDataAlta(LocalDateTime dataAlta) {
        this.dataAlta = dataAlta;
    }

    public String getQuarto() {
        return quarto;
    }

    public void setQuarto(String quarto) {
        this.quarto = quarto;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }
}
