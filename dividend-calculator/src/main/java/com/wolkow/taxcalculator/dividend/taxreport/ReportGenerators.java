package com.wolkow.taxcalculator.dividend.taxreport;

import com.wolkow.taxcalculator.rateprovider.RateProvider;

import java.math.BigDecimal;

public class ReportGenerators {

    public static ReportGenerator createByName(String name, RateProvider rateProvider, BigDecimal taxRate) {
        if ("csv".equalsIgnoreCase(name)) {
            return new CsvReportGenerator(rateProvider, taxRate);
        }
        throw new RuntimeException();
    }
}
