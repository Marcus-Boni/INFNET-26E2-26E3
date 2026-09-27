package com.infnet.shipping.repository;

import com.infnet.shipping.domain.model.Shipment;
import com.infnet.shipping.domain.model.ShipmentStatus;
import com.infnet.shipping.domain.repository.ShipmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ShipmentRepositoryTest {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Test
    @DisplayName("Deve salvar e buscar envio por código de rastreamento")
    void shouldSaveAndFindByTrackingNumber() {
        Shipment shipment = Shipment.builder()
                .orderId(999L)
                .customerEmail("teste@exemplo.com")
                .trackingNumber("NX-123456-BR")
                .carrier("Nexus Express Air")
                .serviceType("EXPRESS")
                .freightCost(new BigDecimal("29.90"))
                .estimatedDeliveryDays(2)
                .destinationStreet("Rua das Flores, 123")
                .destinationCity("Curitiba")
                .destinationState("PR")
                .destinationZipCode("80000-000")
                .status(ShipmentStatus.CREATED)
                .build();

        shipmentRepository.save(shipment);

        Optional<Shipment> found = shipmentRepository.findByTrackingNumber("NX-123456-BR");
        assertThat(found).isPresent();
        assertThat(found.get().getOrderId()).isEqualTo(999L);
        assertThat(found.get().getStatus()).isEqualTo(ShipmentStatus.CREATED);
    }

    @Test
    @DisplayName("Deve buscar envio por orderId")
    void shouldFindByOrderId() {
        Shipment shipment = Shipment.builder()
                .orderId(555L)
                .customerEmail("pedidos@exemplo.com")
                .trackingNumber("NX-555555-BR")
                .carrier("LogBrasil Rodoviário")
                .serviceType("STANDARD")
                .freightCost(new BigDecimal("19.90"))
                .estimatedDeliveryDays(4)
                .destinationStreet("Av. Brasil, 500")
                .destinationCity("Belo Horizonte")
                .destinationState("MG")
                .destinationZipCode("30000-000")
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        shipmentRepository.save(shipment);

        Optional<Shipment> found = shipmentRepository.findByOrderId(555L);
        assertThat(found).isPresent();
        assertThat(found.get().getTrackingNumber()).isEqualTo("NX-555555-BR");
    }
}
