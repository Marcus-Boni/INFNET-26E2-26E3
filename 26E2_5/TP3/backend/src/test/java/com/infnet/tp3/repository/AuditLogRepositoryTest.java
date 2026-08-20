package com.infnet.tp3.repository;

import com.infnet.tp3.domain.model.AuditLog;
import com.infnet.tp3.domain.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    @DisplayName("Deve salvar e listar logs de auditoria por tipo de entidade")
    void shouldSaveAndFindAuditLogsByEntityName() {
        AuditLog log1 = AuditLog.builder()
                .entityName("Order")
                .entityId(1L)
                .action("CREATE")
                .detailDescription("Pedido criado com frete integrado")
                .timestamp(LocalDateTime.now())
                .build();

        AuditLog log2 = AuditLog.builder()
                .entityName("Product")
                .entityId(10L)
                .action("STOCK_CHANGE")
                .detailDescription("Estoque ajustado")
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(log1);
        auditLogRepository.save(log2);

        List<AuditLog> orderLogs = auditLogRepository.findByEntityNameOrderByTimestampDesc("Order");
        assertThat(orderLogs).hasSize(1);
        assertThat(orderLogs.get(0).getEntityId()).isEqualTo(1L);
    }
}
