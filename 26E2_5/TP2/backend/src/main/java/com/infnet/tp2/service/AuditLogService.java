package com.infnet.tp2.service;

import com.infnet.tp2.domain.model.AuditLog;
import com.infnet.tp2.domain.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logChange(String entityName, Long entityId, String action, String description, String previousValue, String newValue) {
        AuditLog log = AuditLog.builder()
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .detailDescription(description)
                .previousValue(previousValue)
                .newValue(newValue)
                .build();
        return auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getHistoryForEntity(String entityName, Long entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc(entityName, entityId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getHistoryForEntityName(String entityName) {
        return auditLogRepository.findByEntityNameOrderByTimestampDesc(entityName);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAllHistory() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
