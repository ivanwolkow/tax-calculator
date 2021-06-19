package com.wolkow.taxcalculator.capitalgains.properties;

import com.wolkow.taxcalculator.capitalgains.taxreport.TaxReportGenerator;
import com.wolkow.taxcalculator.capitalgains.tradeprovider.TradeProvider;
import com.wolkow.taxcalculator.rateprovider.RateProvider;
import lombok.Builder;
import lombok.Getter;

import java.io.File;
import java.math.BigDecimal;
import java.time.ZoneId;

@Getter
@Builder
public class ApplicationProperties {

    /* Common properties */
    private final Integer year;
    private final BigDecimal taxRate;
    private final String taxCurrency;
    private final ZoneId taxTimeZone;

    private final TradeProvider tradeProvider;
    private final TaxReportGenerator taxReportGenerator;
    private final RateProvider rateProvider;

    /* Properties required when using as CLI tool*/
    private final File reportDir;
    private final File outputFile;

}
