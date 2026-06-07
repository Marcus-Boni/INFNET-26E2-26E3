package com.infnet.academia.repository;

import com.infnet.academia.dto.AlunoRankingDTO;
import com.infnet.academia.model.Aluno;
import com.infnet.academia.model.AlunoTreino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlunoTreinoRepository extends JpaRepository<AlunoTreino, Long> {

    @Query("SELECT new com.infnet.academia.dto.AlunoRankingDTO(at.aluno, COUNT(at.id)) " +
           "FROM AlunoTreino at " +
           "WHERE at.statusConclusao = 'CONCLUIDO' " +
           "GROUP BY at.aluno " +
           "ORDER BY COUNT(at.id) DESC")
    List<AlunoRankingDTO> obterRankingAlunos();
}
