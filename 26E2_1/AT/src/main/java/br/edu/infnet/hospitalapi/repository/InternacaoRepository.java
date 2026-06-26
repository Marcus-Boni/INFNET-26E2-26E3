package br.edu.infnet.hospitalapi.repository;

import br.edu.infnet.hospitalapi.model.Internacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InternacaoRepository extends JpaRepository<Internacao, Long> {
}
