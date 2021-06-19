package com.wolkow.taxcalculator.dividend.taxreport;

import com.wolkow.taxcalculator.rateprovider.RateProvider;

import java.math.BigDecimal;

public class TaxReportGenerators {

    public static TaxReportGenerator createByName(String name, RateProvider rateProvider, BigDecimal taxRate) {
        if ("csv".equalsIgnoreCase(name)) {
            return new CsvTaxReportGenerator(rateProvider, taxRate);
        }
        throw new RuntimeException();
    }
}
