package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

/**
 * DerivativeTrade implementation representing option contracts and derivative products.
 * Note: Expiry is validated only against tradeDate, NOT LocalDate.now(), to allow 
 * valid historical expired trades to be processed and reconciled.
 */
public final class DerivativeTrade implements TradeType {

    public enum OptionType { CALL, PUT }

    private final TradeRef tradeRef;
    private final String underlying;
    private final BigDecimal strike;
    private final BigDecimal quantity;
    private final LocalDate expiry;
    private final OptionType optionType;
    private final Currency currency;
    private final Side side;
    private final LocalDate tradeDate;
    private final long counterpartyId;

    private DerivativeTrade(Builder builderInst) {
        this.tradeRef       = builderInst.tradeRef;
        this.underlying     = builderInst.underlying;
        this.strike         = builderInst.strike;
        this.quantity       = builderInst.quantity;
        this.expiry         = builderInst.expiry;
        this.optionType     = builderInst.optionType;
        this.currency       = builderInst.currency;
        this.side           = builderInst.side;
        this.tradeDate      = builderInst.tradeDate;
        this.counterpartyId = builderInst.counterpartyId;
    }

    public static Builder builder() { return new Builder(); }

    @Override public TradeRef tradeRef()     { return tradeRef; }
    @Override public LocalDate tradeDate()   { return tradeDate; }
    @Override public AssetClass assetClass() { return AssetClass.DERIVATIVE; }
    @Override public Money notional()        { return new Money(strike.multiply(quantity), currency); }

    public String underlying()       { return underlying; }
    public BigDecimal strike()       { return strike; }
    public BigDecimal quantity()     { return quantity; }
    public LocalDate expiry()        { return expiry; }
    public OptionType optionType()   { return optionType; }
    public Currency currency()       { return currency; }
    public Side side()               { return side; }
    public long counterpartyId()     { return counterpartyId; }

    public static final class Builder {
        private TradeRef tradeRef;
        private String underlying;
        private BigDecimal strike, quantity;
        private LocalDate expiry, tradeDate;
        private OptionType optionType;
        private Currency currency;
        private Side side;
        private long counterpartyId;

        public Builder tradeRef(TradeRef val)        { this.tradeRef = val; return this; }
        public Builder underlying(String val)        { this.underlying = val; return this; }
        public Builder strike(BigDecimal val)        { this.strike = val; return this; }
        public Builder quantity(BigDecimal val)      { this.quantity = val; return this; }
        public Builder expiry(LocalDate val)          { this.expiry = val; return this; }
        public Builder optionType(OptionType val)    { this.optionType = val; return this; }
        public Builder currency(Currency val)        { this.currency = val; return this; }
        public Builder currency(String isoCodeStr)   { this.currency = Currency.getInstance(isoCodeStr); return this; }
        public Builder side(Side val)                { this.side = val; return this; }
        public Builder tradeDate(LocalDate val)      { this.tradeDate = val; return this; }
        public Builder counterpartyId(long val)      { this.counterpartyId = val; return this; }

        public DerivativeTrade build() {
            Objects.requireNonNull(tradeRef,   "tradeRef");
            Objects.requireNonNull(underlying, "underlying");
            Objects.requireNonNull(strike,     "strike");
            Objects.requireNonNull(quantity,   "quantity");
            Objects.requireNonNull(expiry,     "expiry");
            Objects.requireNonNull(optionType, "optionType");
            Objects.requireNonNull(currency,   "currency");
            Objects.requireNonNull(side,       "side");
            Objects.requireNonNull(tradeDate,  "tradeDate");

            if (strike.signum() <= 0) {
                throw new IllegalStateException("strike must be > 0");
            }
            if (quantity.signum() <= 0) {
                throw new IllegalStateException("quantity must be > 0");
            }
            if (expiry.isBefore(tradeDate)) {
                throw new IllegalStateException("expiry cannot be before tradeDate");
            }

            return new DerivativeTrade(this);
        }
    }
}
