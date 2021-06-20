package com.wolkow.taxcalculator.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DividendRequest {

    private Integer taxYear;
    private String taxCurrency;
    private BigDecimal taxRate;

    private String brokerType;
    private String reportType;
}
