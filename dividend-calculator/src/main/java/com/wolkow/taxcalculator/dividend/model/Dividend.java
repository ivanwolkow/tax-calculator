package com.wolkow.taxcalculator.dividend.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class Dividend {
    private LocalDate date;
    private String ticker;
    private String currency;
    private String country;
    private int quantity;
    private BigDecimal gross;       //total accrual amount, positive value
    private BigDecimal withhold;    //withhold by broker, negative value
}
