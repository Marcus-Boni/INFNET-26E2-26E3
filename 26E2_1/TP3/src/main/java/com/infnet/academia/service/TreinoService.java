package com.infnet.academia.service;

import com.infnet.academia.model.Instrutor;
import com.infnet.academia.model.Treino;
import com.infnet.academia.repository.InstrutorRepository;
import com.infnet.academia.repository.TreinoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TreinoService {

    private final TreinoRepository treinoRepository;
    private final InstrutorRepository instrutorRepository;

    public TreinoService(TreinoRepository treinoRepository, InstrutorRepository instrutorRepository) {
        this.treinoRepository = treinoRepository;
        this.instrutorRepository = instrutorRepository;
    }

    @Transactional
    public Treino cadastrarTreino(String nome, String focoPrincipal, Long instrutorId) {
        Instrutor instrutor = instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new IllegalArgumentException("Instrutor com ID " + instrutorId + " não encontrado."));

        Treino treino = Treino.builder()
                .nome(nome)
                .focoPrincipal(focoPrincipal)
                .instrutor(instrutor)
                .build();

        return treinoRepository.save(treino);
    }
}
