package br.edu.infnet.hospitalapi.repository;

import br.edu.infnet.hospitalapi.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    @Query("SELECT m FROM Medico m ORDER BY SIZE(m.consultas) DESC")
    List<Medico> findMedicosOrderedByConsultasCount();
}
