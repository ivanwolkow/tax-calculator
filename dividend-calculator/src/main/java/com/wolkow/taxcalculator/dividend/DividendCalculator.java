package com.wolkow.taxcalculator.dividend;

import com.wolkow.taxcalculator.dividend.divprovider.DivProvider;
import com.wolkow.taxcalculator.dividend.model.Dividend;
import com.wolkow.taxcalculator.dividend.taxreport.TaxReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.io.Reader;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DividendCalculator {

    private final DivProvider divProvider;
    private final TaxReportGenerator reportGenerator;

    @SneakyThrows
    public void generateTaxReport(Appendable destination, Integer taxYear, Reader... sources) {
        log.info("Searching for dividend accruals...");

        List<Dividend> dividends = StreamEx.of(divProvider.readDivs(sources))
                .filterBy(div -> div.getDate().getYear(), taxYear)
                .sortedBy(Dividend::getDate)
                .toList();

        log.info("Found {} dividends in year {}", dividends.size(), taxYear);
        dividends.forEach(div -> log.info(div.toString()));

        reportGenerator.generateReport(destination, dividends);
    }

}
