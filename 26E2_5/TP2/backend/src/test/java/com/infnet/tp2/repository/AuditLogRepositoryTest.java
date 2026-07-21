package com.infnet.tp2.repository;

import com.infnet.tp2.domain.model.AuditLog;
import com.infnet.tp2.domain.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();

        auditLogRepository.save(AuditLog.builder()
                .entityName("Product")
                .entityId(1L)
                .action("PRICE_CHANGE")
                .detailDescription("Alteração de preço")
                .previousValue("R$ 100,00")
                .newValue("R$ 120,00")
                .build());

        auditLogRepository.save(AuditLog.builder()
                .entityName("Order")
                .entityId(10L)
                .action("STATUS_CHANGE")
                .detailDescription("Envio do pedido")
                .previousValue("PENDING")
                .newValue("SHIPPED")
                .build());
    }

    @Test
    @DisplayName("Deve buscar logs de auditoria por nome e id da entidade")
    void shouldFindByEntityNameAndEntityId() {
        List<AuditLog> logs = auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc("Product", 1L);
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getAction()).isEqualTo("PRICE_CHANGE");
    }

    @Test
    @DisplayName("Deve buscar todos os logs ordenados por timestamp decrescente")
    void shouldFindAllByOrderByTimestampDesc() {
        List<AuditLog> allLogs = auditLogRepository.findAllByOrderByTimestampDesc();
        assertThat(allLogs).hasSize(2);
    }
}
