package br.edu.infnet.hospitalapi.service;

import br.edu.infnet.hospitalapi.dto.PacienteRequestDTO;
import br.edu.infnet.hospitalapi.dto.PacienteResponseDTO;
import br.edu.infnet.hospitalapi.exception.ResourceNotFoundException;
import br.edu.infnet.hospitalapi.model.Paciente;
import br.edu.infnet.hospitalapi.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteService pacienteService;

    private Paciente paciente;
    private PacienteRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        paciente = new Paciente(1L, "João Silva", "123.456.789-00", LocalDate.of(1985, 3, 20), "(11) 98765-4321");
        requestDTO = new PacienteRequestDTO("João Silva", "123.456.789-00", LocalDate.of(1985, 3, 20), "(11) 98765-4321");
    }

    @Test
    void testCadastrarPaciente() {
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        PacienteResponseDTO response = pacienteService.cadastrarPaciente(requestDTO);

        assertNotNull(response);
        assertEquals(paciente.getId(), response.getId());
        assertEquals(paciente.getNome(), response.getNome());
        assertEquals(paciente.getCpf(), response.getCpf());
        assertEquals(paciente.getDataNascimento(), response.getDataNascimento());
        assertEquals(paciente.getTelefone(), response.getTelefone());

        verify(pacienteRepository, times(1)).save(any(Paciente.class));
    }

    @Test
    void testBuscarPorIdSuccess() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        PacienteResponseDTO response = pacienteService.buscarPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("João Silva", response.getNome());

        verify(pacienteRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorIdNotFound() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pacienteService.buscarPorId(99L));

        verify(pacienteRepository, times(1)).findById(99L);
    }

    @Test
    void testRemoverPacienteSuccess() {
        when(pacienteRepository.existsById(1L)).thenReturn(true);
        doNothing().when(pacienteRepository).deleteById(1L);

        assertDoesNotThrow(() -> pacienteService.removerPaciente(1L));

        verify(pacienteRepository, times(1)).existsById(1L);
        verify(pacienteRepository, times(1)).deleteById(1L);
    }

    @Test
    void testRemoverPacienteNotFound() {
        when(pacienteRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> pacienteService.removerPaciente(99L));

        verify(pacienteRepository, times(1)).existsById(99L);
        verify(pacienteRepository, never()).deleteById(anyLong());
    }
}
