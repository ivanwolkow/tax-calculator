package com.wolkow.taxcalculator.capitalgains.taxreport;

import com.wolkow.taxcalculator.capitalgains.model.Gain;
import com.wolkow.taxcalculator.capitalgains.model.Trade;
import com.wolkow.taxcalculator.capitalgains.properties.ApplicationProperties;
import com.wolkow.taxcalculator.rateprovider.RateProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.UP;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.LF;

@Slf4j
public class DefaultTaxReportGenerator implements TaxReportGenerator {

    private final ApplicationProperties properties;
    private final RateProvider rateProvider;

    public DefaultTaxReportGenerator(ApplicationProperties properties,
                                     RateProvider rateProvider) {
        this.properties = properties;
        this.rateProvider = rateProvider;
    }

    @Override
    @SneakyThrows
    public void generateReport(List<Gain> gains) {
        var output = properties.getOutputFile();
        output.getParentFile().mkdirs();

        var printer = new CSVPrinter(new FileWriter(output, UTF_8),
                CSVFormat.DEFAULT.withRecordSeparator(LF));

        printer.printRecord(
                "txn time",
                "ticker",
                "txn currency",
                "price per share",
                "quantity",
                "txn total",
                "txn conversion rate",
                "txn in " + properties.getTaxCurrency(),
                "fee",
                "fee currency",
                "fee conversion rate",
                "fee in " + properties.getTaxCurrency(),
                "total in " + properties.getTaxCurrency()
        );

        BigDecimal totalInTaxCurrency = StreamEx.of(gains)
                .map(gain -> processGain(gain, printer))
                .reduce(ZERO, BigDecimal::add);

        BigDecimal taxToPay = (totalInTaxCurrency.compareTo(ZERO) >= 0)
                ? ZERO
                : totalInTaxCurrency.multiply(properties.getTaxRate()).scaleByPowerOfTen(-2);

        printer.printRecord(createRecord("TOTAL PROFIT", emptyFields(11),
                totalInTaxCurrency.setScale(2, UP)));

        printer.printRecord(createRecord("TAX RATE", emptyFields(11), properties.getTaxRate() + "%"));

        printer.printRecord(createRecord("TAX TO PAY", emptyFields(11),
                taxToPay.setScale(2, UP)));

        printer.flush();
        printer.close();
    }

    @SneakyThrows
    private BigDecimal processGain(Gain gain, CSVPrinter printer) {
        BigDecimal totalBuyInTaxCurrency = StreamEx.of(gain.getBuys())
                .map(buy -> processTradeEntry(buy, printer))
                .reduce(ZERO, BigDecimal::add);

        BigDecimal totalSellInTaxCurrency = processTradeEntry(gain.getSell(), printer);
        BigDecimal totalInTaxCurrency = totalBuyInTaxCurrency.add(totalSellInTaxCurrency);

        printer.printRecord(createRecord("TOTAL", emptyFields(11), totalInTaxCurrency));
        printer.printRecord();

        return totalInTaxCurrency;
    }

    @SneakyThrows
    private BigDecimal processTradeEntry(Trade trade, CSVPrinter printer) {
        BigDecimal transaction = trade.getPricePerShare().multiply(new BigDecimal(trade.getQuantity()));
        BigDecimal transactionRate = rateProvider.getRateByDateAndCurrency(trade.getTime().toLocalDate(), trade.getCurrency());
        BigDecimal transactionInTaxCurrency = transaction.multiply(transactionRate);

        BigDecimal feeRate = rateProvider.getRateByDateAndCurrency(trade.getTime().toLocalDate(), trade.getFeeCurrency());
        BigDecimal feeInTaxCurrency = trade.getFee().multiply(feeRate);

        BigDecimal totalInTaxCurrency = transactionInTaxCurrency.subtract(feeInTaxCurrency);

        printer.printRecord(
                trade.getTime(),
                trade.getTicker(),
                trade.getCurrency(),
                trade.getPricePerShare(),
                trade.getQuantity(),
                transaction,
                transactionRate,
                transactionInTaxCurrency,
                trade.getFee(),
                trade.getFeeCurrency(),
                feeRate,
                feeInTaxCurrency,
                totalInTaxCurrency
        );

        return totalInTaxCurrency;
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
