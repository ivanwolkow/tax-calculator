package com.wolkow.taxcalculator.dividend;

import com.wolkow.taxcalculator.dividend.divprovider.DivProvider;
import com.wolkow.taxcalculator.dividend.model.Dividend;
import com.wolkow.taxcalculator.dividend.taxreport.ReportGenerator;
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
    private final ReportGenerator reportGenerator;

    @SneakyThrows
    public void generateReport(Appendable destination, Integer taxYear, Reader... sources) {
        List<Dividend> dividends = StreamEx.of(divProvider.readDivs(sources))
                .filterBy(div -> div.getDate().getYear(), taxYear)
                .sortedBy(Dividend::getDate)
                .toList();

        log.info("Found {} dividends in year {}", dividends.size(), taxYear);
        dividends.forEach(div -> log.debug(div.toString()));

        reportGenerator.generateReport(destination, dividends);
    }

}
