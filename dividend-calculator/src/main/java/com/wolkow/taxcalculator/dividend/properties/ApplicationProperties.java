package com.wolkow.taxcalculator.dividend.properties;

import com.wolkow.taxcalculator.dividend.divprovider.DivProvider;
import com.wolkow.taxcalculator.dividend.taxreport.TaxReportGenerator;
import com.wolkow.taxcalculator.rateprovider.RateProvider;
import lombok.Builder;
import lombok.Getter;

import java.io.File;
import java.math.BigDecimal;

@Getter
@Builder
public class ApplicationProperties {

    /* Common properties */
    private final Integer year;
    private final BigDecimal taxRate;
    private final String taxCurrency;

    private final DivProvider divProvider;
    private final RateProvider rateProvider;
    private final TaxReportGenerator taxReportGenerator;

    /* Properties required when using as CLI tool*/
    private final File reportDir;
    private final File outputFile;

}
