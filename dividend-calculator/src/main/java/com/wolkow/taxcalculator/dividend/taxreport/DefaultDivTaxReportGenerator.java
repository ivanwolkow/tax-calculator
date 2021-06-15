package com.wolkow.taxcalculator.dividend.taxreport;

import com.wolkow.taxcalculator.dividend.model.Dividend;
import com.wolkow.taxcalculator.dividend.properties.ApplicationProperties;
import com.wolkow.taxcalculator.rateprovider.RateProvider;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.LF;

public class DefaultDivTaxReportGenerator implements DivTaxReportGenerator {

    private final ApplicationProperties properties;
    private final RateProvider rateProvider;

    public DefaultDivTaxReportGenerator(ApplicationProperties properties, RateProvider rateProvider) {
        this.properties = properties;
        this.rateProvider = rateProvider;
    }

    @Override
    @SneakyThrows
    public void generateReport(List<Dividend> dividends) {
        var output = properties.getOutputFile();
        output.getParentFile().mkdirs();

        var printer = new CSVPrinter(new FileWriter(output, UTF_8),
                CSVFormat.DEFAULT.withRecordSeparator(LF));

        printer.printRecord(
                "date",
                "ticker",
                "currency",
                "country",
                "quantity",
                "conversion rate",
                "gross",
                "gross in " + properties.getTaxCurrency(),
                "tax rate",
                "total tax in " + properties.getTaxCurrency(),
                "withhold by broker",
                "withhold in " + properties.getTaxCurrency(),
                "yet to pay in " + properties.getTaxCurrency()
        );

        BigDecimal totalInTaxCurrency = StreamEx.of(dividends)
                .map(div -> processDividend(div, printer))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        printer.printRecord(createRecord("TOTAL", emptyFields(11), totalInTaxCurrency));

        printer.flush();
        printer.close();
    }

    @SneakyThrows
    private BigDecimal processDividend(Dividend dividend, CSVPrinter printer) {
        BigDecimal rate = rateProvider.getRateByDateAndCurrency(dividend.getDate(), dividend.getCurrency());
        BigDecimal grossInTaxCurrency = dividend.getGross().multiply(rate);
        BigDecimal totalTaxInTaxCurrency = grossInTaxCurrency.multiply(properties.getTaxRate()).scaleByPowerOfTen(-2);
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
                properties.getTaxRate() + "%",
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
