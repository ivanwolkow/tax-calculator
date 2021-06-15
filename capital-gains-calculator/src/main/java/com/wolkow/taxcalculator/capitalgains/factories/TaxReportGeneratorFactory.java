package com.wolkow.taxcalculator.capitalgains.factories;

import com.wolkow.taxcalculator.capitalgains.enums.TaxReportGeneratorType;
import com.wolkow.taxcalculator.capitalgains.properties.ApplicationProperties;
import com.wolkow.taxcalculator.capitalgains.taxreport.DefaultTaxReportGenerator;
import com.wolkow.taxcalculator.capitalgains.taxreport.TaxReportGenerator;
import com.wolkow.taxcalculator.rateprovider.RateProvider;

public class TaxReportGeneratorFactory {

    public static TaxReportGenerator createTaxReportGenerator(ApplicationProperties properties, RateProvider rateProvider) {
        if (properties.getTaxReportGenerator() == TaxReportGeneratorType.DEFAULT_TAX_REPORT_GENERATOR) {
            return new DefaultTaxReportGenerator(properties, rateProvider);
        }
        throw new RuntimeException("Tax report generator not supported");
    }
}
