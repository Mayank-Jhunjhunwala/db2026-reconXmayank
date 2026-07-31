package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

/**
 * FXTrade implementation representing foreign exchange transactions.
 * Notional currency convention: Base amount in ccy1, converted via fxRate into ccy2.
 */
public final class FXTrade implements TradeType {

    private final TradeRef tradeRef;
    private final Currency ccy1;
    private final Currency ccy2;
    private final BigDecimal notionalCcy1;
    private final BigDecimal fxRate;
    private final Side side;
    private final LocalDate tradeDate;
    private final long counterpartyId;

    private FXTrade(Builder builderInst) {
        this.tradeRef       = builderInst.tradeRef;
        this.ccy1           = builderInst.ccy1;
        this.ccy2           = builderInst.ccy2;
        this.notionalCcy1   = builderInst.notionalCcy1;
        this.fxRate         = builderInst.fxRate;
        this.side           = builderInst.side;
        this.tradeDate      = builderInst.tradeDate;
        this.counterpartyId = builderInst.counterpartyId;
    }

    public static Builder builder() { return new Builder(); }

    @Override public TradeRef tradeRef()     { return tradeRef; }
    @Override public LocalDate tradeDate()   { return tradeDate; }
    @Override public AssetClass assetClass() { return AssetClass.FX; }
    @Override public Money notional()        { return new Money(notionalCcy1.multiply(fxRate), ccy2); }

    public Currency ccy1()           { return ccy1; }
    public Currency ccy2()           { return ccy2; }
    public BigDecimal notionalCcy1() { return notionalCcy1; }
    public BigDecimal fxRate()       { return fxRate; }
    public Side side()               { return side; }
    public long counterpartyId()     { return counterpartyId; }

    public static final class Builder {
        private TradeRef tradeRef;
        private Currency ccy1, ccy2;
        private BigDecimal notionalCcy1, fxRate;
        private Side side;
        private LocalDate tradeDate;
        private long counterpartyId;

        public Builder tradeRef(TradeRef val)        { this.tradeRef = val; return this; }
        public Builder ccy1(String isoCodeStr)       { this.ccy1 = Currency.getInstance(isoCodeStr); return this; }
        public Builder ccy2(String isoCodeStr)       { this.ccy2 = Currency.getInstance(isoCodeStr); return this; }
        public Builder notionalCcy1(BigDecimal val) { this.notionalCcy1 = val; return this; }
        public Builder fxRate(BigDecimal val)        { this.fxRate = val; return this; }
        public Builder side(Side val)                { this.side = val; return this; }
        public Builder tradeDate(LocalDate val)      { this.tradeDate = val; return this; }
        public Builder counterpartyId(long val)      { this.counterpartyId = val; return this; }

        public FXTrade build() {
            Objects.requireNonNull(tradeRef,     "tradeRef");
            Objects.requireNonNull(ccy1,         "ccy1");
            Objects.requireNonNull(ccy2,         "ccy2");
            Objects.requireNonNull(notionalCcy1, "notionalCcy1");
            Objects.requireNonNull(fxRate,       "fxRate");
            Objects.requireNonNull(side,         "side");
            Objects.requireNonNull(tradeDate,    "tradeDate");

            if (ccy1.equals(ccy2)) {
                throw new IllegalStateException("ccy1 and ccy2 must differ");
            }
            if (notionalCcy1.signum() <= 0) {
                throw new IllegalStateException("notionalCcy1 must be > 0");
            }
            if (fxRate.signum() <= 0) {
                throw new IllegalStateException("fxRate must be > 0");
            }

            return new FXTrade(this);
        }
    }
    @Override public boolean equals(Object o) {
        return (o instanceof FXTrade other) && tradeRef.equals(other.tradeRef);
    }
    @Override public int hashCode() { return tradeRef.hashCode(); }
    @Override public String toString() {
        return "FXTrade[ref=%s, %s/%s, notional=%s %s, rate=%s, side=%s]"
                .formatted(tradeRef, ccy1.getCurrencyCode(), ccy2.getCurrencyCode(),
                           notionalCcy1, ccy1.getCurrencyCode(), fxRate, side);
    }
}
