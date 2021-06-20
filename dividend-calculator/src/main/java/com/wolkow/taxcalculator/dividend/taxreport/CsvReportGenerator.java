package com.wolkow.taxcalculator.dividend.taxreport;

import com.wolkow.taxcalculator.dividend.model.Dividend;
import com.wolkow.taxcalculator.rateprovider.RateProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.math.RoundingMode.UP;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.LF;

@Slf4j
public class CsvReportGenerator implements ReportGenerator {

    private final RateProvider rateProvider;
    private final BigDecimal taxRate;

    public CsvReportGenerator(RateProvider rateProvider, BigDecimal taxRate) {
        this.rateProvider = rateProvider;
        this.taxRate = taxRate;
    }

    @Override
    public String getName() {
        return "csv";
    }

    @Override
    @SneakyThrows
    public void generateReport(Appendable appendable, List<Dividend> dividends) {
        var printer = new CSVPrinter(appendable, CSVFormat.DEFAULT.withRecordSeparator(LF));

        printer.printRecord(
                "date",
                "ticker",
                "currency",
                "country",
                "quantity",
                "conversion rate",
                "gross",
                "gross in tax currency",
                "tax rate",
                "total tax in tax currency",
                "withhold by broker",
                "withhold in tax currency",
                "yet to pay in tax currency"
        );

        BigDecimal totalInTaxCurrency = StreamEx.of(dividends)
                .map(div -> processDividend(printer, div))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        printer.printRecord(createRecord("TOTAL", emptyFields(11), totalInTaxCurrency.setScale(2, UP)));

        printer.flush();
        printer.close();

        log.info("Total dividend tax yet to pay: {}", totalInTaxCurrency.setScale(2, UP));
    }

    @SneakyThrows
    private BigDecimal processDividend(CSVPrinter printer, Dividend dividend) {
        BigDecimal rate = rateProvider.getRateByDateAndCurrency(dividend.getDate(), dividend.getCurrency());
        BigDecimal grossInTaxCurrency = dividend.getGross().multiply(rate);
        BigDecimal totalTaxInTaxCurrency = grossInTaxCurrency.multiply(taxRate).scaleByPowerOfTen(-2);
        BigDecimal withholdInTaxCurrency = dividend.getWithhold().multiply(rate);
        BigDecimal yetToPayInTaxCurrency = totalTaxInTaxCurrency.add(withholdInTaxCurrency).max(BigDecimal.ZERO);

        printer.printRecord(dividend.getDate(),
                dividend.getTicker(),
                dividend.getCurrency(),
                dividend.getCountry(),
                dividend.getQuantity(),
                rate,
                dividend.getGross(),
                grossInTaxCurrency,
                taxRate + "%",
                totalTaxInTaxCurrency,
                dividend.getWithhold(),
                withholdInTaxCurrency,
                yetToPayInTaxCurrency
        );

        return yetToPayInTaxCurrency;
    }

    private static List<String> createRecord(Object... objects) {
        ArrayList<String> record = new ArrayList<>();
        for (Object o : objects) {
            if (o instanceof Iterable) {
                ((Iterable<?>) o).forEach(i -> record.add(i.toString()));
                continue;
            }
            record.add(o.toString());
        }
        return record;
    }

    private static List<String> emptyFields(int n) {
        return Collections.nCopies(n, EMPTY);
    }
}
