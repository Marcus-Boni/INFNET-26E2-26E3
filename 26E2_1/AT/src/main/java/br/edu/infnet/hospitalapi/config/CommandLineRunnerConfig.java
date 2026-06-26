package br.edu.infnet.hospitalapi.config;

import br.edu.infnet.hospitalapi.model.Medico;
import br.edu.infnet.hospitalapi.model.Paciente;
import br.edu.infnet.hospitalapi.repository.MedicoRepository;
import br.edu.infnet.hospitalapi.repository.PacienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;

@Configuration
@Profile("!test") // Do not run initial data seeding during tests to keep test DB state predictable
public class CommandLineRunnerConfig implements CommandLineRunner {

    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;

    public CommandLineRunnerConfig(PacienteRepository pacienteRepository, MedicoRepository medicoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed Doctors
        if (medicoRepository.count() == 0) {
            Medico cardiologista = new Medico("Dr. Carlos Silva", "CRM-12345", "Cardiologista");
            Medico ortopedista = new Medico("Dr. André Souza", "CRM-67890", "Ortopedista");
            medicoRepository.save(cardiologista);
            medicoRepository.save(ortopedista);
            System.out.println("CommandLineRunner: Médicos iniciais inseridos com sucesso.");
        }

        // Seed Patients
        if (pacienteRepository.count() == 0) {
            Paciente joao = new Paciente("João Silva", "123.456.789-00", LocalDate.of(1985, 3, 20), "(11) 98765-4321");
            Paciente maria = new Paciente("Maria Oliveira", "987.654.321-11", LocalDate.of(1992, 7, 10), "(21) 99999-8888");
            pacienteRepository.save(joao);
            pacienteRepository.save(maria);
            System.out.println("CommandLineRunner: Pacientes iniciais inseridos com sucesso.");
        }
    }
}
