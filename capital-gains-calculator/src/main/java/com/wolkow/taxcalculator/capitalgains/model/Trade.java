package com.wolkow.taxcalculator.capitalgains.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class Trade {
    private String assetCategory;
    private String currency;
    private String ticker;
    private LocalDateTime time;         //transaction time (in tax timezone)
    private BigDecimal pricePerShare;   //always positive
    private int quantity;               //positive for buys, negative for sells
    private BigDecimal fee;             //negative value
    private String feeCurrency;

    public Trade clone(int quantity) {
        return Trade.builder()
                .assetCategory(this.assetCategory)
                .currency(this.currency)
                .ticker(this.ticker)
                .time(this.time)
                .pricePerShare(this.pricePerShare)
                .quantity(quantity)
                .fee(this.fee)
                .feeCurrency(this.feeCurrency)
                .build();
    }

    @Override
    public String toString() {
        return "Trade{" +
                "time=" + time +
                ", quantity=" + quantity +
                '}';
    }
}
