package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class TradeAnalyticsServiceTest {
    private final TradeAnalyticsService service = new TradeAnalyticsService();

    @Test
    void notionalByCounterparty_groupsCorrectly() {
        var t1 = equity("EQU-20260603-0001", "100.00", "10", 1L);
        var t2 = equity("EQU-20260603-0002", "200.00", "5", 1L);
        var t3 = equity("EQU-20260603-0003", "50.00", "20", 2L);
        var result = service.notionalByCounterparty(List.of(t1, t2, t3));
        assertThat(result).hasSize(2);
        assertThat(result.get(1L).count()).isEqualTo(2);
        assertThat(result.get(1L).total()).isEqualByComparingTo(new BigDecimal("2000"));
        assertThat(result.get(2L).count()).isEqualTo(1);
        assertThat(result.get(2L).total()).isEqualByComparingTo(new BigDecimal("1000"));
    }

    private EquityTrade equity(String ref, String price, String qty, long cpId) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref)).instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price)).quantity(new BigDecimal(qty))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3)).counterpartyId(cpId).build();
    }

    @Test
    void vwapByInstrument_computesCorrectly() {
        var t1 = equity("EQU-20260603-0010", "100.00", "100", 1L);
        var t2 = equity("EQU-20260603-0011", "110.00", "200", 1L);
        var result = service.vwapByInstrument(List.of(t1, t2));
        assertThat(result.get("SAP.DE")).isEqualByComparingTo(new BigDecimal("106.6667"));
    }

    @Test
    void vwapByInstrument_emptyReturnsEmpty() {
        assertThat(service.vwapByInstrument(List.of())).isEmpty();
    }
}
