package com.infnet.shipping.repository;

import com.infnet.shipping.domain.model.ShipmentStatus;
import com.infnet.shipping.domain.model.TrackingEvent;
import com.infnet.shipping.domain.repository.TrackingEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TrackingEventRepositoryTest {

    @Autowired
    private TrackingEventRepository trackingEventRepository;

    @Test
    @DisplayName("Deve salvar e listar eventos de rastreio em ordem cronológica")
    void shouldSaveAndFindEventsChronologically() {
        LocalDateTime t1 = LocalDateTime.now().minusHours(5);
        LocalDateTime t2 = LocalDateTime.now().minusHours(2);

        TrackingEvent e1 = TrackingEvent.builder()
                .shipmentId(10L)
                .trackingNumber("NX-999-BR")
                .status(ShipmentStatus.CREATED)
                .message("Postado")
                .location("Origem SP")
                .timestamp(t1)
                .build();

        TrackingEvent e2 = TrackingEvent.builder()
                .shipmentId(10L)
                .trackingNumber("NX-999-BR")
                .status(ShipmentStatus.DISPATCHED)
                .message("Despachado")
                .location("Hub Cajamar")
                .timestamp(t2)
                .build();

        trackingEventRepository.save(e1);
        trackingEventRepository.save(e2);

        List<TrackingEvent> events = trackingEventRepository.findByShipmentIdOrderByTimestampAsc(10L);
        assertThat(events).hasSize(2);
        assertThat(events.get(0).getStatus()).isEqualTo(ShipmentStatus.CREATED);
        assertThat(events.get(1).getStatus()).isEqualTo(ShipmentStatus.DISPATCHED);
    }
}
