package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

public final class EquityTrade implements TradeType {

    private final TradeRef tradeRef;
    private final String instrumentSymbol;
    private final BigDecimal quantity;
    private final BigDecimal price;
    private final Currency currency;
    private final Side side;
    private final LocalDate tradeDate;
    private final long counterpartyId;

    private EquityTrade(Builder builderInst) {
        this.tradeRef         = builderInst.tradeRef;
        this.instrumentSymbol = builderInst.instrumentSymbol;
        this.quantity         = builderInst.quantity;
        this.price            = builderInst.price;
        this.currency         = builderInst.currency;
        this.side             = builderInst.side;
        this.tradeDate        = builderInst.tradeDate;
        this.counterpartyId   = builderInst.counterpartyId;
    }

    public static Builder builder() { return new Builder(); }

    @Override public TradeRef tradeRef()     { return tradeRef; }
    @Override public LocalDate tradeDate()   { return tradeDate; }
    @Override public AssetClass assetClass() { return AssetClass.EQUITY; }
    @Override public Money notional()        { return new Money(quantity.multiply(price), currency); }

    public String instrumentSymbol() { return instrumentSymbol; }
    public BigDecimal quantity()      { return quantity; }
    public BigDecimal price()         { return price; }
    public Currency currency()        { return currency; }
    public Side side()                { return side; }
    public long counterpartyId()      { return counterpartyId; }

    public static final class Builder {
        private TradeRef tradeRef;
        private String instrumentSymbol;
        private BigDecimal quantity;
        private BigDecimal price;
        private Currency currency;
        private Side side;
        private LocalDate tradeDate;
        private long counterpartyId;

        public Builder tradeRef(TradeRef val)           { this.tradeRef = val;          return this; }
        public Builder instrumentSymbol(String val)     { this.instrumentSymbol = val; return this; }
        public Builder quantity(BigDecimal val)         { this.quantity = val;          return this; }
        public Builder price(BigDecimal val)            { this.price = val;             return this; }
        public Builder currency(Currency val)           { this.currency = val;          return this; }
        public Builder currency(String currencyCodeStr) { return currency(Currency.getInstance(currencyCodeStr)); }
        public Builder side(Side val)                   { this.side = val;              return this; }
        public Builder tradeDate(LocalDate val)         { this.tradeDate = val;         return this; }
        public Builder counterpartyId(long val)         { this.counterpartyId = val;    return this; }

        public EquityTrade build() {
            Objects.requireNonNull(tradeRef,         "tradeRef");
            Objects.requireNonNull(instrumentSymbol, "instrumentSymbol");
            Objects.requireNonNull(quantity,         "quantity");
            Objects.requireNonNull(price,            "price");
            Objects.requireNonNull(currency,         "currency");
            Objects.requireNonNull(side,             "side");
            Objects.requireNonNull(tradeDate,        "tradeDate");
            if (quantity.signum() <= 0) throw new IllegalStateException("quantity must be > 0");
            if (price.signum() <= 0)    throw new IllegalStateException("price must be > 0");
            return new EquityTrade(this);
        }
    }
}
