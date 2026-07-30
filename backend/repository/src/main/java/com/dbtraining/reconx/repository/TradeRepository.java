package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.domain.Trade;
import com.dbtraining.reconx.domain.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TradeRepository
        extends JpaRepository<Trade, Long>, JpaSpecificationExecutor<Trade> {

    Optional<Trade> findByTradeRef(String tradeRef);

    @Query("""
        SELECT t FROM Trade t
        WHERE t.tradeDate BETWEEN :startDate AND :endDate
          AND (:tradeStatus IS NULL OR t.status = :tradeStatus)
          AND (:cpId IS NULL OR t.counterparty.id = :cpId)
        """)
    Page<Trade> findByFilters(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("tradeStatus") TradeStatus tradeStatus,
        @Param("cpId") Long cpId,
        Pageable pageable
    );
}
