package com.infnet.shipping.infrastructure.config;

import com.infnet.shipping.domain.model.Shipment;
import com.infnet.shipping.domain.model.ShipmentStatus;
import com.infnet.shipping.domain.model.TrackingEvent;
import com.infnet.shipping.domain.repository.ShipmentRepository;
import com.infnet.shipping.domain.repository.TrackingEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class ShippingDataLoader implements CommandLineRunner {

    private final ShipmentRepository shipmentRepository;
    private final TrackingEventRepository trackingEventRepository;

    @Autowired
    public ShippingDataLoader(ShipmentRepository shipmentRepository, TrackingEventRepository trackingEventRepository) {
        this.shipmentRepository = shipmentRepository;
        this.trackingEventRepository = trackingEventRepository;
    }

    @Override
    public void run(String... args) {
        if (shipmentRepository.count() == 0) {
            // Seed Shipment 1: In Transit
            Shipment s1 = Shipment.builder()
                    .orderId(101L)
                    .customerEmail("carlos.silva@exemplo.com")
                    .trackingNumber("NX-749102-BR")
                    .carrier("Nexus Express Air")
                    .serviceType("EXPRESS")
                    .freightCost(new BigDecimal("24.60"))
                    .estimatedDeliveryDays(2)
                    .destinationStreet("Av. Paulista, 1000, Apto 42")
                    .destinationCity("São Paulo")
                    .destinationState("SP")
                    .destinationZipCode("01310-100")
                    .status(ShipmentStatus.IN_TRANSIT)
                    .build();

            Shipment savedS1 = shipmentRepository.save(s1);

            trackingEventRepository.save(TrackingEvent.builder()
                    .shipmentId(savedS1.getId())
                    .trackingNumber(savedS1.getTrackingNumber())
                    .status(ShipmentStatus.CREATED)
                    .message("Objeto postado e etiqueta emitida.")
                    .location("Centro de Distribuição Principal - SP")
                    .timestamp(LocalDateTime.now().minusHours(24))
                    .build());

            trackingEventRepository.save(TrackingEvent.builder()
                    .shipmentId(savedS1.getId())
                    .trackingNumber(savedS1.getTrackingNumber())
                    .status(ShipmentStatus.DISPATCHED)
                    .message("Objeto despachado para a unidade de tratamento.")
                    .location("Hub Logístico Cajamar / SP")
                    .timestamp(LocalDateTime.now().minusHours(18))
                    .build());

            trackingEventRepository.save(TrackingEvent.builder()
                    .shipmentId(savedS1.getId())
                    .trackingNumber(savedS1.getTrackingNumber())
                    .status(ShipmentStatus.IN_TRANSIT)
                    .message("Em trânsito para a unidade de distribuição local.")
                    .location("Centro Operacional São Paulo Capital")
                    .timestamp(LocalDateTime.now().minusHours(6))
                    .build());

            // Seed Shipment 2: Delivered
            Shipment s2 = Shipment.builder()
                    .orderId(102L)
                    .customerEmail("mariana.costa@exemplo.com")
                    .trackingNumber("NX-982314-BR")
                    .carrier("LogBrasil Rodoviário")
                    .serviceType("STANDARD")
                    .freightCost(new BigDecimal("18.50"))
                    .estimatedDeliveryDays(3)
                    .destinationStreet("Rua das Laranjeiras, 350")
                    .destinationCity("Rio de Janeiro")
                    .destinationState("RJ")
                    .destinationZipCode("22240-006")
                    .status(ShipmentStatus.DELIVERED)
                    .build();

            Shipment savedS2 = shipmentRepository.save(s2);

            trackingEventRepository.save(TrackingEvent.builder()
                    .shipmentId(savedS2.getId())
                    .trackingNumber(savedS2.getTrackingNumber())
                    .status(ShipmentStatus.CREATED)
                    .message("Etiqueta gerada pelo e-commerce.")
                    .location("Centro de Distribuição Principal - SP")
                    .timestamp(LocalDateTime.now().minusDays(3))
                    .build());

            trackingEventRepository.save(TrackingEvent.builder()
                    .shipmentId(savedS2.getId())
                    .trackingNumber(savedS2.getTrackingNumber())
                    .status(ShipmentStatus.DISPATCHED)
                    .message("Despachado via transporte rodoviário.")
                    .location("Hub Cajamar / SP")
                    .timestamp(LocalDateTime.now().minusDays(2))
                    .build());

            trackingEventRepository.save(TrackingEvent.builder()
                    .shipmentId(savedS2.getId())
                    .trackingNumber(savedS2.getTrackingNumber())
                    .status(ShipmentStatus.OUT_FOR_DELIVERY)
                    .message("Objeto saiu para entrega ao destinatário.")
                    .location("Centro de Distribuição Domiciliar - Botafogo / RJ")
                    .timestamp(LocalDateTime.now().minusHours(8))
                    .build());

            trackingEventRepository.save(TrackingEvent.builder()
                    .shipmentId(savedS2.getId())
                    .trackingNumber(savedS2.getTrackingNumber())
                    .status(ShipmentStatus.DELIVERED)
                    .message("Objeto entregue ao destinatário com sucesso.")
                    .location("Rio de Janeiro / RJ")
                    .timestamp(LocalDateTime.now().minusHours(2))
                    .build());

            System.out.println("Shipping Service: Banco de dados populado com " + shipmentRepository.count() + " envios e trilhas de rastreio de demonstração.");
        }
    }
}
