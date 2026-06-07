package com.infnet.academia.repository;

import com.infnet.academia.model.AvaliacaoFisica;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvaliacaoFisicaRepository extends MongoRepository<AvaliacaoFisica, String> {
    List<AvaliacaoFisica> findByAlunoId(Long alunoId);
}
