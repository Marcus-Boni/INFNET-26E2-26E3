package br.edu.infnet.hospitalapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medicos")
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "CRM é obrigatório")
    @Column(nullable = false, unique = true)
    private String crm;

    @NotBlank(message = "Especialidade é obrigatória")
    @Column(nullable = false)
    private String specialty; // mapping to CRM-defined specialty or simple string

    @OneToMany(mappedBy = "medico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consulta> consultas = new ArrayList<>();

    public Medico() {
    }

    public Medico(Long id, String nome, String crm, String specialty) {
        this.id = id;
        this.nome = nome;
        this.crm = crm;
        this.specialty = specialty;
    }

    public Medico(String nome, String crm, String specialty) {
        this.nome = nome;
        this.crm = crm;
        this.specialty = specialty;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCrm() {
        return crm;
    }

    public void setCrm(String crm) {
        this.crm = crm;
    }

    public String getEspecialidade() {
        return specialty;
    }

    public void setEspecialidade(String specialty) {
        this.specialty = specialty;
    }

    public List<Consulta> getConsultas() {
        return consultas;
    }

    public void setConsultas(List<Consulta> consultas) {
        this.consultas = consultas;
    }
}
