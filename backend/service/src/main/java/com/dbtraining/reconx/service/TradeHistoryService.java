package com.dbtraining.reconx.service;

import com.dbtraining.reconx.domain.Trade;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TradeHistoryService {

    private final EntityManager entityManager;

    public TradeHistoryService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<Number> revisionsFor(Long tradeId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.getRevisions(Trade.class, tradeId);
    }

    @Transactional(readOnly = true)
    public Trade snapshotAt(Long tradeId, Number revisionNumber) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.find(Trade.class, tradeId, revisionNumber);
    }
}
