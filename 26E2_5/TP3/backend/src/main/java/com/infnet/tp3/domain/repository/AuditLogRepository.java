package com.infnet.tp3.domain.repository;

import com.infnet.tp3.domain.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, Long entityId);

    List<AuditLog> findByEntityNameOrderByTimestampDesc(String entityName);

    List<AuditLog> findAllByOrderByTimestampDesc();
}
