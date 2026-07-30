package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReconciliationEngineTest {

    private final ReconciliationEngine engine = new ReconciliationEngine();

    @Test
    @DisplayName("exact match on price and qty returns MATCHED")
    void testReconcile_exactMatch_returnsMatched() {
        EquityTrade internal = equity("EQU-20260603-0001", "100.00", "1000");
        EquityTrade external = equity("EQU-20260603-0001", "100.00", "1000");
        List<ReconResult> out = engine.reconcile(List.of(internal), List.of(external), ReconciliationRule.EXACT);
        assertThat(out).hasSize(1);
        assertThat(out.get(0).status()).isEqualTo(ReconResult.Status.MATCHED);
    }

    @ParameterizedTest(name = "price diff {0} stays within 1% tolerance -> MATCHED")
    @ValueSource(strings = {"0.10", "0.50", "0.99"})
    void testReconcile_priceTolerance_withinThreshold(String diff) {
        EquityTrade internal = equity("EQU-20260603-0002", "100.00", "1000");
        EquityTrade external = equity("EQU-20260603-0002",
                new BigDecimal("100.00").add(new BigDecimal(diff)).toString(), "1000");
        List<ReconResult> out = engine.reconcile(List.of(internal), List.of(external),
                ReconciliationRule.PRICE_TOLERANCE_1PCT);
        assertThat(out.get(0).status()).isEqualTo(ReconResult.Status.MATCHED);
    }

    @Test
    @DisplayName("missing external trade produces BREAK with MISSING_EXTERNAL")
    void testReconcile_missingCounterpartyTrade_returnsBreak() {
        EquityTrade internal = equity("EQU-20260603-0003", "100.00", "1000");
        List<ReconResult> out = engine.reconcile(List.of(internal), List.of(), ReconciliationRule.EXACT);
        assertThat(out.get(0).status()).isEqualTo(ReconResult.Status.BREAK);
        assertThat(out.get(0).discrepancyType()).isEqualTo("MISSING_EXTERNAL");
    }

    @Test
    @DisplayName("empty internal list returns empty result")
    void testReconcile_emptyInternal_returnsEmpty() {
        assertThat(engine.reconcile(List.of(), List.of(), ReconciliationRule.EXACT)).isEmpty();
    }

    @Test
    @DisplayName("single internal with no external returns BREAK MISSING_EXTERNAL")
    void testReconcile_singleInternalNoExternal_returnsBreak() {
        EquityTrade internal = equity("EQU-20260603-0099", "100.00", "1000");
        List<ReconResult> out = engine.reconcile(List.of(internal), List.of(), ReconciliationRule.EXACT);
        assertThat(out).hasSize(1);
        assertThat(out.get(0).status()).isEqualTo(ReconResult.Status.BREAK);
        assertThat(out.get(0).discrepancyType()).isEqualTo("MISSING_EXTERNAL");
    }

    @Test
    @DisplayName("null internal returns empty result")
    void testReconcile_nullInternal_returnsEmpty() {
        assertThat(engine.reconcile(null, List.of(), ReconciliationRule.EXACT)).isEmpty();
    }

    @Test
    @DisplayName("null external is treated as empty - all internal become BREAK")
    void testReconcile_nullExternal_allBreak() {
        EquityTrade internal = equity("EQU-20260603-0098", "100.00", "1000");
        List<ReconResult> out = engine.reconcile(List.of(internal), null, ReconciliationRule.EXACT);
        assertThat(out).hasSize(1);
        assertThat(out.get(0).status()).isEqualTo(ReconResult.Status.BREAK);
    }

    private EquityTrade equity(String ref, String price, String qty) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
