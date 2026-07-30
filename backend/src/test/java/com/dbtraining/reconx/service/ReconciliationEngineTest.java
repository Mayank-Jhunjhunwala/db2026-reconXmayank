package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TICKET-ADV040 / ADV041 / ADV042 — TDD: write the test FIRST, then the impl.
 */
class ReconciliationEngineTest {

    private final ReconciliationEngine engine = new ReconciliationEngine();

    @org.junit.jupiter.api.DisplayName("exact match on price and qty returns MATCHED")
    @org.junit.jupiter.api.Test
    void testReconcile_exactMatch_returnsMatched() {
        // given
        EquityTrade internal = equity("EQU-20260603-0001", "100.00", "1000");
        EquityTrade external = equity("EQU-20260603-0001", "100.00", "1000");
        // when
        java.util.List<com.dbtraining.reconx.dto.ReconResult> out = engine.reconcile(java.util.List.of(internal), java.util.List.of(external), com.dbtraining.reconx.model.ReconciliationRule.EXACT);
        // then
        org.assertj.core.api.Assertions.assertThat(out).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(out.get(0).status()).isEqualTo(com.dbtraining.reconx.dto.ReconResult.Status.MATCHED);
    }

    @Test
    void testReconcile_priceTolerance_withinThreshold() {
        // TODO(TICKET-ADV041): prices 100.00 vs 100.50 + PRICE_TOLERANCE_1PCT rule -> status MATCHED.
        org.junit.jupiter.api.Assertions.fail("TICKET-ADV041 not implemented yet");
    }

    @Test
    void testReconcile_missingCounterpartyTrade_returnsBreak() {
        // TODO(TICKET-ADV042): internal trade with no external counterpart -> status BREAK,
        //                     discrepancyType = "MISSING_EXTERNAL".
        org.junit.jupiter.api.Assertions.fail("TICKET-ADV042 not implemented yet");
    }

    @org.junit.jupiter.api.Test
    void testReconcile_emptyInternal_returnsEmpty() {
        // TODO(TICKET-ADV047): empty internal + empty external -> reconcile returns an empty list.
        org.junit.jupiter.api.Assertions.fail("TICKET-ADV047 not implemented yet");
    }

    private EquityTrade equity(String ref, String price, String qty) {
        return EquityTrade.builder()
                .tradeRef(com.dbtraining.reconx.model.TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
