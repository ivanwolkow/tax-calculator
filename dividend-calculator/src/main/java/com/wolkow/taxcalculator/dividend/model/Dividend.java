package com.wolkow.taxcalculator.dividend.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class Dividend {
    private final LocalDate date;
    private final String ticker;
    private final String currency;
    private final String country;
    private final int quantity;
    private final BigDecimal gross;       //total accrual amount, positive value
    private final BigDecimal withhold;    //withhold by broker, negative value

    @Override
    public String toString() {
        return "{date=" + date +
                ", ticker='" + ticker + '\'' +
                ", currency='" + currency + '\'' +
                ", quantity=" + quantity +
                ", gross=" + gross +
                ", withhold=" + withhold +
                '}';
    }
}
