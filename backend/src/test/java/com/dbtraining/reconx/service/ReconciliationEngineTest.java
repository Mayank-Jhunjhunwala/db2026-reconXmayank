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

    @org.junit.jupiter.params.ParameterizedTest(name = "price diff {0} stays within 1% tolerance -> MATCHED")
    @org.junit.jupiter.params.provider.ValueSource(strings = {"0.10", "0.50", "0.99"})
    void testReconcile_priceTolerance_withinThreshold(String diff) {
        EquityTrade internal = equity("EQU-20260603-0002", "100.00", "1000");
        java.math.BigDecimal basePrice = new java.math.BigDecimal("100.00");
        EquityTrade external = equity("EQU-20260603-0002", basePrice.add(new java.math.BigDecimal(diff)).toString(), "1000");

        java.util.List<com.dbtraining.reconx.dto.ReconResult> out = engine.reconcile(java.util.List.of(internal), java.util.List.of(external),
                com.dbtraining.reconx.model.ReconciliationRule.PRICE_TOLERANCE_1PCT);

        org.assertj.core.api.Assertions.assertThat(out.get(0).status()).isEqualTo(com.dbtraining.reconx.dto.ReconResult.Status.MATCHED);
    }

    @org.junit.jupiter.api.Test
    void testReconcile_missingCounterpartyTrade_returnsBreak() {
        EquityTrade internal = equity("EQU-20260603-0003", "100.00", "1000");

        java.util.List<com.dbtraining.reconx.dto.ReconResult> out = engine.reconcile(java.util.List.of(internal), java.util.List.of(), com.dbtraining.reconx.model.ReconciliationRule.EXACT);

        org.assertj.core.api.Assertions.assertThat(out.get(0).status()).isEqualTo(com.dbtraining.reconx.dto.ReconResult.Status.BREAK);
        org.assertj.core.api.Assertions.assertThat(out.get(0).reason()).isEqualTo("MISSING_EXTERNAL");
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
