package br.edu.infnet.hospitalapi.service;

import br.edu.infnet.hospitalapi.dto.MedicoRequestDTO;
import br.edu.infnet.hospitalapi.dto.MedicoResponseDTO;
import br.edu.infnet.hospitalapi.model.Medico;
import br.edu.infnet.hospitalapi.repository.MedicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MedicoServiceTest {

    @Mock
    private MedicoRepository medicoRepository;

    @InjectMocks
    private MedicoService medicoService;

    private Medico medico;
    private MedicoRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        medico = new Medico(1L, "Dr. Carlos Silva", "CRM-12345", "Cardiologista");
        requestDTO = new MedicoRequestDTO("Dr. Carlos Silva", "CRM-12345", "Cardiologista");
    }

    @Test
    void testCadastrarMedico() {
        when(medicoRepository.save(any(Medico.class))).thenReturn(medico);

        MedicoResponseDTO response = medicoService.cadastrarMedico(requestDTO);

        assertNotNull(response);
        assertEquals(medico.getId(), response.getId());
        assertEquals(medico.getNome(), response.getNome());
        assertEquals(medico.getCrm(), response.getCrm());
        assertEquals(medico.getEspecialidade(), response.getEspecialidade());

        verify(medicoRepository, times(1)).save(any(Medico.class));
    }
}
