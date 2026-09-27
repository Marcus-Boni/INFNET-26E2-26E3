package com.infnet.shipping.domain.repository;

import com.infnet.shipping.domain.model.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    List<TrackingEvent> findByShipmentIdOrderByTimestampAsc(Long shipmentId);

    List<TrackingEvent> findByTrackingNumberOrderByTimestampAsc(String trackingNumber);
}
