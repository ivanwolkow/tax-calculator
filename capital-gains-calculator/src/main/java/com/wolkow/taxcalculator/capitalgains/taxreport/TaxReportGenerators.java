package com.wolkow.taxcalculator.capitalgains.taxreport;

import com.wolkow.taxcalculator.rateprovider.RateProvider;

import java.math.BigDecimal;
import java.time.ZoneId;

public class TaxReportGenerators {

    public static TaxReportGenerator createByName(String name, RateProvider rateProvider, BigDecimal taxRate, ZoneId taxZone) {
        if ("csv".equalsIgnoreCase(name)) return new CsvTaxReportGenerator(rateProvider, taxRate, taxZone);
        throw new RuntimeException();
    }
}
