package com.infnet.shipping.domain.repository;

import com.infnet.shipping.domain.model.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {
    List<EventLog> findAllByOrderByTimestampDesc();
    List<EventLog> findByCorrelationIdOrderByTimestampAsc(String correlationId);
    List<EventLog> findByEventTypeOrderByTimestampDesc(String eventType);
    Optional<EventLog> findByEventId(String eventId);
}
