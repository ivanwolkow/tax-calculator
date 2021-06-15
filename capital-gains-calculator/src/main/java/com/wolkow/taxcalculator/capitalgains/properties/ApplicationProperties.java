package com.wolkow.taxcalculator.capitalgains.properties;

import com.wolkow.taxcalculator.capitalgains.enums.TaxReportGeneratorType;
import com.wolkow.taxcalculator.capitalgains.enums.TradeProviderType;
import com.wolkow.taxcalculator.rateprovider.enums.RateProviderType;
import lombok.Builder;
import lombok.Getter;

import java.io.File;
import java.math.BigDecimal;
import java.time.ZoneId;

@Getter
@Builder
public class ApplicationProperties {

    /* Common properties */
    private Integer year;
    private File reportDir;
    private File outputFile;
    private BigDecimal taxRate;
    private String taxCurrency;
    private ZoneId taxTimeZone;

    /* Switching between different input/output formats */
    private RateProviderType rateProvider;
    private TradeProviderType tradeProvider;
    private TaxReportGeneratorType taxReportGenerator;
}
