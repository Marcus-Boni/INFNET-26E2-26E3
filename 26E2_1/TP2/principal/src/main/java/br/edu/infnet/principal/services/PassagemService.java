package br.edu.infnet.principal.services;

import br.edu.infnet.principal.dtos.PassagemRequestDTO;
import br.edu.infnet.principal.dtos.PassagemResponseDTO;
import br.edu.infnet.principal.models.Passagem;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PassagemService {

    private List<Passagem> passagens = new ArrayList<>();
    private Long idCounter = 1L;

    public PassagemService() {
        Passagem p1 = new Passagem(idCounter++, "Carlos Silva", 5, "Rio de Janeiro", "São Paulo", LocalDate.now().plusDays(2), "CONFIRMADA");
        Passagem p2 = new Passagem(idCounter++, "Ana Souza", 8, "São Paulo", "Belo Horizonte", LocalDate.now().plusDays(5), "PENDENTE");
        Passagem p3 = new Passagem(idCounter++, "Marcos Oliveira", 12, "Belo Horizonte", "Vitória", LocalDate.now().plusDays(10), "CANCELADA");

        passagens.add(p1);
        passagens.add(p2);
        passagens.add(p3);
    }

    public Passagem convertToEntity(PassagemRequestDTO dto) {
        Passagem passagem = new Passagem();
        passagem.setPassageiro(dto.getPassageiro());
        passagem.setAssento(dto.getAssento());
        passagem.setOrigem(dto.getOrigem());
        passagem.setDestino(dto.getDestino());
        passagem.setData(dto.getData());
        passagem.setStatus(dto.getStatus());
        return passagem;
    }

    public PassagemResponseDTO convertToDTO(Passagem passagem) {
        PassagemResponseDTO dto = new PassagemResponseDTO();
        dto.setId(passagem.getId());
        dto.setPassageiro(passagem.getPassageiro());
        dto.setAssento(passagem.getAssento());
        dto.setOrigem(passagem.getOrigem());
        dto.setDestino(passagem.getDestino());
        dto.setData(passagem.getData());
        dto.setStatus(passagem.getStatus());
        return dto;
    }

    public List<PassagemResponseDTO> findAll() {
        return passagens.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PassagemResponseDTO findById(Long id) {
        Passagem passagem = passagens.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Passagem não encontrada com o ID: " + id));
        return convertToDTO(passagem);
    }

    public PassagemResponseDTO create(PassagemRequestDTO dto) {
        boolean assentoOcupado = passagens.stream()
                .anyMatch(p -> p.getAssento().equals(dto.getAssento()));
        if (assentoOcupado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O assento " + dto.getAssento() + " já está reservado.");
        }

        Passagem passagem = convertToEntity(dto);
        passagem.setId(idCounter++);
        passagens.add(passagem);

        return convertToDTO(passagem);
    }

    public PassagemResponseDTO update(Long id, PassagemRequestDTO dto) {
        Passagem passagem = passagens.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Passagem não encontrada com o ID: " + id));

        boolean assentoOcupado = passagens.stream()
                .anyMatch(p -> p.getAssento().equals(dto.getAssento()) && !p.getId().equals(id));
        if (assentoOcupado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O assento " + dto.getAssento() + " já está reservado por outra passagem.");
        }

        passagem.setPassageiro(dto.getPassageiro());
        passagem.setAssento(dto.getAssento());
        passagem.setOrigem(dto.getOrigem());
        passagem.setDestino(dto.getDestino());
        passagem.setData(dto.getData());
        passagem.setStatus(dto.getStatus());

        return convertToDTO(passagem);
    }

    public void delete(Long id) {
        boolean removed = passagens.removeIf(p -> p.getId().equals(id));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Passagem não encontrada com o ID: " + id);
        }
    }

    public List<PassagemResponseDTO> findByDestino(String destino) {
        return passagens.stream()
                .filter(p -> p.getDestino().equalsIgnoreCase(destino))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
