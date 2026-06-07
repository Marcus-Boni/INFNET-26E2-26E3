package com.infnet.academia.config;

import com.infnet.academia.model.*;
import com.infnet.academia.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final PlanoRepository planoRepository;
    private final InstrutorRepository instrutorRepository;
    private final AlunoRepository alunoRepository;
    private final TreinoRepository treinoRepository;
    private final AlunoTreinoRepository alunoTreinoRepository;

    public DatabaseSeeder(PlanoRepository planoRepository,
                          InstrutorRepository instrutorRepository,
                          AlunoRepository alunoRepository,
                          TreinoRepository treinoRepository,
                          AlunoTreinoRepository alunoTreinoRepository) {
        this.planoRepository = planoRepository;
        this.instrutorRepository = instrutorRepository;
        this.alunoRepository = alunoRepository;
        this.treinoRepository = treinoRepository;
        this.alunoTreinoRepository = alunoTreinoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("====== INICIANDO CARGA DE DADOS (DATABASE SEEDER) ======");

        // 1. Inserir Planos
        Plano basico = Plano.builder()
                .nome("Básico")
                .valor(new BigDecimal("90.00"))
                .build();

        Plano premium = Plano.builder()
                .nome("Premium")
                .valor(new BigDecimal("150.00"))
                .build();

        planoRepository.saveAll(List.of(basico, premium));
        System.out.println("Planos criados: Básico e Premium.");

        // 2. Inserir Instrutores
        Instrutor silva = Instrutor.builder()
                .nome("Instrutor Silva")
                .cref("123456-G/RJ")
                .build();

        Instrutor santos = Instrutor.builder()
                .nome("Instrutor Santos")
                .cref("654321-G/RJ")
                .build();

        instrutorRepository.saveAll(List.of(silva, santos));
        System.out.println("Instrutores criados: Silva e Santos.");

        // 3. Inserir Alunos (Ativos e Inativos para teste do Exercício 8)
        Aluno marcus = Aluno.builder()
                .nome("Marcus Boni")
                .email("marcus@infnet.edu.br")
                .dataNascimento(LocalDate.of(2000, 5, 15))
                .ativo(true)
                .plano(premium)
                .build();

        Aluno ana = Aluno.builder()
                .nome("Ana Souza")
                .email("ana@gmail.com")
                .dataNascimento(LocalDate.of(1995, 10, 22))
                .ativo(true)
                .plano(basico)
                .build();

        Aluno pedro = Aluno.builder()
                .nome("Pedro Oliveira")
                .email("pedro@outlook.com")
                .dataNascimento(LocalDate.of(1998, 2, 8))
                .ativo(false) // Inativo
                .plano(basico)
                .build();

        Aluno julia = Aluno.builder()
                .nome("Julia Costa")
                .email("julia@yahoo.com")
                .dataNascimento(LocalDate.of(2002, 12, 1))
                .ativo(true)
                .plano(premium)
                .build();

        alunoRepository.saveAll(List.of(marcus, ana, pedro, julia));
        System.out.println("Alunos criados: Marcus, Ana, Julia (Ativos) e Pedro (Inativo).");

        // 4. Inserir Treinos vinculados aos Instrutores
        Treino treinoForca = Treino.builder()
                .nome("Treino de Hipertrofia")
                .focoPrincipal("Força Muscular")
                .instrutor(silva)
                .build();

        Treino treinoCardio = Treino.builder()
                .nome("Treino de Resistência Cardiorrespiratória")
                .focoPrincipal("Cardio/Resistência")
                .instrutor(santos)
                .build();

        Treino treinoPerna = Treino.builder()
                .nome("Treino de Inferiores")
                .focoPrincipal("Membros Inferiores")
                .instrutor(silva)
                .build();

        treinoRepository.saveAll(List.of(treinoForca, treinoCardio, treinoPerna));
        System.out.println("Treinos criados: Hipertrofia, Resistência Cardiorrespiratória, Inferiores.");

        // 5. Inserir Conclusões para o Ranking de Alunos (Exercício 9)
        // Marcus conclui 3 treinos
        alunoTreinoRepository.save(AlunoTreino.builder()
                .aluno(marcus)
                .treino(treinoForca)
                .dataInicio(LocalDate.now().minusDays(5))
                .statusConclusao("CONCLUIDO")
                .build());
        alunoTreinoRepository.save(AlunoTreino.builder()
                .aluno(marcus)
                .treino(treinoCardio)
                .dataInicio(LocalDate.now().minusDays(3))
                .statusConclusao("CONCLUIDO")
                .build());
        alunoTreinoRepository.save(AlunoTreino.builder()
                .aluno(marcus)
                .treino(treinoPerna)
                .dataInicio(LocalDate.now().minusDays(1))
                .statusConclusao("CONCLUIDO")
                .build());

        // Julia conclui 2 treinos
        alunoTreinoRepository.save(AlunoTreino.builder()
                .aluno(julia)
                .treino(treinoForca)
                .dataInicio(LocalDate.now().minusDays(4))
                .statusConclusao("CONCLUIDO")
                .build());
        alunoTreinoRepository.save(AlunoTreino.builder()
                .aluno(julia)
                .treino(treinoCardio)
                .dataInicio(LocalDate.now().minusDays(2))
                .statusConclusao("CONCLUIDO")
                .build());

        // Ana conclui 1 treino, e tem outro em andamento
        alunoTreinoRepository.save(AlunoTreino.builder()
                .aluno(ana)
                .treino(treinoForca)
                .dataInicio(LocalDate.now().minusDays(4))
                .statusConclusao("CONCLUIDO")
                .build());
        alunoTreinoRepository.save(AlunoTreino.builder()
                .aluno(ana)
                .treino(treinoCardio)
                .dataInicio(LocalDate.now().minusDays(1))
                .statusConclusao("EM_ANDAMENTO") // Não conta como concluído no ranking
                .build());

        System.out.println("Registros de AlunoTreino semeados para Ranking.");
        System.out.println("====== CARGA DE DADOS FINALIZADA COM SUCESSO ======");
    }
}
