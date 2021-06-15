package com.wolkow.taxcalculator.dividend.properties;

import com.wolkow.taxcalculator.dividend.enums.DivProviderType;
import com.wolkow.taxcalculator.dividend.enums.TaxReportGeneratorType;
import com.wolkow.taxcalculator.rateprovider.enums.RateProviderType;
import lombok.Builder;
import lombok.Getter;

import java.io.File;
import java.math.BigDecimal;

@Getter
@Builder
public class ApplicationProperties {

    /* Common properties */
    private Integer year;
    private File reportDir;
    private File outputFile;
    private BigDecimal taxRate;
    private String taxCurrency;

    /* Switching between different input/output formats */
    private RateProviderType rateProvider;
    private DivProviderType divProvider;
    private TaxReportGeneratorType taxReportGenerator;
}
