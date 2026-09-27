package com.infnet.shipping.domain.repository;

import com.infnet.shipping.domain.model.Shipment;
import com.infnet.shipping.domain.model.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    Optional<Shipment> findByOrderId(Long orderId);

    List<Shipment> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);

    List<Shipment> findByStatusOrderByCreatedAtDesc(ShipmentStatus status);

    List<Shipment> findAllByOrderByCreatedAtDesc();
}
