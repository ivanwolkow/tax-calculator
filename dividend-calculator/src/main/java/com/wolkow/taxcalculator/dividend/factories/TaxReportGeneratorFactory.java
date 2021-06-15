package com.wolkow.taxcalculator.dividend.factories;

import com.wolkow.taxcalculator.dividend.enums.TaxReportGeneratorType;
import com.wolkow.taxcalculator.dividend.properties.ApplicationProperties;
import com.wolkow.taxcalculator.dividend.taxreport.DefaultDivTaxReportGenerator;
import com.wolkow.taxcalculator.dividend.taxreport.DivTaxReportGenerator;
import com.wolkow.taxcalculator.rateprovider.RateProvider;

public class TaxReportGeneratorFactory {

    public static DivTaxReportGenerator createTaxReportGenerator(ApplicationProperties properties, RateProvider rateProvider) {
        if (properties.getTaxReportGenerator() == TaxReportGeneratorType.DEFAULT_TAX_REPORT_GENERATOR) {
            return new DefaultDivTaxReportGenerator(properties, rateProvider);
        }
        throw new RuntimeException("Tax report generator not supported");
    }
}
