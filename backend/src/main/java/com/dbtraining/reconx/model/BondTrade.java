package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

/**
 * BondTrade implementation representing fixed-income securities.
 * Face value and coupon rates use BigDecimal exclusively (no floating-point types).
 */
public final class BondTrade implements TradeType {

    private final TradeRef tradeRef;
    private final String isin;
    private final BigDecimal faceValue;
    private final BigDecimal couponRate;
    private final LocalDate maturityDate;
    private final Currency currency;
    private final Side side;
    private final LocalDate tradeDate;
    private final long counterpartyId;

    private BondTrade(Builder builderInst) {
        this.tradeRef       = builderInst.tradeRef;
        this.isin           = builderInst.isin;
        this.faceValue      = builderInst.faceValue;
        this.couponRate     = builderInst.couponRate;
        this.maturityDate   = builderInst.maturityDate;
        this.currency       = builderInst.currency;
        this.side           = builderInst.side;
        this.tradeDate      = builderInst.tradeDate;
        this.counterpartyId = builderInst.counterpartyId;
    }

    public static Builder builder() { return new Builder(); }

    @Override public TradeRef tradeRef()     { return tradeRef; }
    @Override public LocalDate tradeDate()   { return tradeDate; }
    @Override public AssetClass assetClass() { return AssetClass.BOND; }
    @Override public Money notional()        { return new Money(faceValue, currency); }

    public String isin()             { return isin; }
    public BigDecimal faceValue()    { return faceValue; }
    public BigDecimal couponRate()   { return couponRate; }
    public LocalDate maturityDate()  { return maturityDate; }
    public Currency currency()       { return currency; }
    public Side side()               { return side; }
    public long counterpartyId()     { return counterpartyId; }

    public static final class Builder {
        private TradeRef tradeRef;
        private String isin;
        private BigDecimal faceValue, couponRate;
        private LocalDate maturityDate, tradeDate;
        private Currency currency;
        private Side side;
        private long counterpartyId;

        public Builder tradeRef(TradeRef val)        { this.tradeRef = val; return this; }
        public Builder isin(String val)              { this.isin = val; return this; }
        public Builder faceValue(BigDecimal val)     { this.faceValue = val; return this; }
        public Builder couponRate(BigDecimal val)    { this.couponRate = val; return this; }
        public Builder maturityDate(LocalDate val)   { this.maturityDate = val; return this; }
        public Builder currency(Currency val)        { this.currency = val; return this; }
        public Builder currency(String isoCodeStr)   { this.currency = Currency.getInstance(isoCodeStr); return this; }
        public Builder side(Side val)                { this.side = val; return this; }
        public Builder tradeDate(LocalDate val)      { this.tradeDate = val; return this; }
        public Builder counterpartyId(long val)      { this.counterpartyId = val; return this; }

        public BondTrade build() {
            Objects.requireNonNull(tradeRef,     "tradeRef");
            Objects.requireNonNull(isin,         "isin");
            Objects.requireNonNull(faceValue,    "faceValue");
            Objects.requireNonNull(couponRate,   "couponRate");
            Objects.requireNonNull(maturityDate, "maturityDate");
            Objects.requireNonNull(currency,     "currency");
            Objects.requireNonNull(side,         "side");
            Objects.requireNonNull(tradeDate,    "tradeDate");

            if (isin.length() != 12) {
                throw new IllegalStateException("ISIN must be exactly 12 characters long");
            }
            if (maturityDate.isBefore(tradeDate) || maturityDate.isEqual(tradeDate)) {
                throw new IllegalStateException("maturityDate cannot be before tradeDate");
            }
            if (faceValue.signum() <= 0) {
                throw new IllegalStateException("faceValue must be > 0");
            }

            return new BondTrade(this);
        }
    }
    @Override public boolean equals(Object o) {
        return (o instanceof BondTrade other) && tradeRef.equals(other.tradeRef);
    }
    @Override public int hashCode() { return tradeRef.hashCode(); }
    @Override public String toString() {
        return "BondTrade[ref=%s, isin=%s, face=%s %s, coupon=%s, maturity=%s, side=%s]"
                .formatted(tradeRef, isin, faceValue, currency.getCurrencyCode(),
                           couponRate, maturityDate, side);
    }
}
