package br.edu.infnet.hospitalapi.integration;

import br.edu.infnet.hospitalapi.dto.PacienteRequestDTO;
import br.edu.infnet.hospitalapi.model.Paciente;
import br.edu.infnet.hospitalapi.repository.PacienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PacienteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        pacienteRepository.deleteAll();
    }

    @Test
    void testPacienteLifecycle() throws Exception {
        // --- Test 1: Cadastrar um paciente através da API ---
        PacienteRequestDTO requestDTO = new PacienteRequestDTO(
                "Lucas Lima",
                "222.333.444-55",
                LocalDate.of(1995, 8, 25),
                "(31) 98888-7777"
        );

        MvcResult result = mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Lucas Lima")))
                .andExpect(jsonPath("$.cpf", is("222.333.444-55")))
                .andExpect(jsonPath("$.dataNascimento", is("1995-08-25")))
                .andExpect(jsonPath("$.telefone", is("(31) 98888-7777")))
                .andReturn();

        // Validate persistence in DB
        String responseContent = result.getResponse().getContentAsString();
        Long createdId = objectMapper.readTree(responseContent).get("id").asLong();
        
        Optional<Paciente> persistedOpt = pacienteRepository.findById(createdId);
        assertTrue(persistedOpt.isPresent());
        assertEquals("Lucas Lima", persistedOpt.get().getNome());

        // --- Test 2: Buscar o paciente cadastrado ---
        mockMvc.perform(get("/api/pacientes/{id}", createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdId.intValue())))
                .andExpect(jsonPath("$.nome", is("Lucas Lima")))
                .andExpect(jsonPath("$.cpf", is("222.333.444-55")));

        // --- Test 3: Listar todos os pacientes ---
        // Add another patient to make list have size 2
        Paciente otherPaciente = new Paciente("Renata Costa", "555.666.777-88", LocalDate.of(1988, 12, 5), "(11) 97777-6666");
        pacienteRepository.save(otherPaciente);

        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome", anyOf(is("Lucas Lima"), is("Renata Costa"))))
                .andExpect(jsonPath("$[1].nome", anyOf(is("Lucas Lima"), is("Renata Costa"))));

        // --- Test 4: Excluir um paciente ---
        mockMvc.perform(delete("/api/pacientes/{id}", createdId))
                .andExpect(status().isNoContent());

        // Validate persistence (it should be deleted)
        assertFalse(pacienteRepository.findById(createdId).isPresent());
        assertEquals(1, pacienteRepository.count()); // only Renata should remain
    }
}
