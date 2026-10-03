package com.exemplo.fornecedoresservice.config;

import com.exemplo.fornecedoresservice.model.Fornecedor;
import com.exemplo.fornecedoresservice.repository.FornecedorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Cadastra cinco fornecedores automaticamente assim que a aplicacao sobe.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final FornecedorRepository fornecedorRepository;

    public DataInitializer(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    @Override
    public void run(String... args) {
        if (fornecedorRepository.count() == 0) {
            fornecedorRepository.save(new Fornecedor("Tech Distribuidora LTDA", "11.222.333/0001-01"));
            fornecedorRepository.save(new Fornecedor("Silicon Componentes Eletronicos S.A.", "22.333.444/0001-02"));
            fornecedorRepository.save(new Fornecedor("Global Hardware & Logistica", "33.444.555/0001-03"));
            fornecedorRepository.save(new Fornecedor("ByteSupply Suprimentos Tecnologicos", "44.555.666/0001-04"));
            fornecedorRepository.save(new Fornecedor("MegaCircuit Importacao e Exportacao", "55.666.777/0001-05"));
        }
    }
}
