package com.wolkow.taxcalculator.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.ZoneId;

@Data
public class CapitalGainsRequest {

    private Integer taxYear;
    private String taxCurrency;
    private BigDecimal taxRate;
    private ZoneId taxZone;

    private String brokerType;
    private String reportType;
}
