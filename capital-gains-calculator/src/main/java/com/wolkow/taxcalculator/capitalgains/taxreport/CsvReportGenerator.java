package com.wolkow.taxcalculator.capitalgains.taxreport;

import com.wolkow.taxcalculator.capitalgains.model.Gain;
import com.wolkow.taxcalculator.capitalgains.model.Trade;
import com.wolkow.taxcalculator.rateprovider.RateProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.UP;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.LF;

@Slf4j
public class CsvReportGenerator implements ReportGenerator {

    private final RateProvider rateProvider;
    private final BigDecimal taxRate;
    private final ZoneId taxZone;

    public CsvReportGenerator(RateProvider rateProvider, BigDecimal taxRate, ZoneId taxZone) {
        this.rateProvider = rateProvider;
        this.taxRate = taxRate;
        this.taxZone = taxZone;
    }

    @Override
    @SneakyThrows
    public void generateReport(Appendable appendable, List<Gain> gains) {
        var printer = new CSVPrinter(appendable, CSVFormat.DEFAULT.withRecordSeparator(LF));

        printer.printRecord(
                "txn time in tax timezone",
                "ticker",
                "txn currency",
                "price per share",
                "quantity",
                "txn total",
                "txn conversion rate",
                "txn in tax currency",
                "fee",
                "fee currency",
                "fee conversion rate",
                "fee in tax currency",
                "total in tax currency"
        );

        BigDecimal totalInTaxCurrency = StreamEx.of(gains)
                .map(gain -> processGain(gain, rateProvider, printer))
                .reduce(ZERO, BigDecimal::add);

        BigDecimal taxToPay = (totalInTaxCurrency.compareTo(ZERO) <= 0)
                ? ZERO
                : totalInTaxCurrency.multiply(taxRate).scaleByPowerOfTen(-2);

        printer.printRecord(createRecord("TOTAL PROFIT", emptyFields(11),
                totalInTaxCurrency.setScale(2, UP)));

        printer.printRecord(createRecord("TAX RATE", emptyFields(11), taxRate + "%"));

        printer.printRecord(createRecord("TAX TO PAY", emptyFields(11),
                taxToPay.setScale(2, UP)));

        printer.flush();
        printer.close();

        log.info("Report successfully generated. Total capital gains tax yet to pay: {}", taxToPay.setScale(2, UP));
    }

    @SneakyThrows
    private BigDecimal processGain(Gain gain, RateProvider rateProvider, CSVPrinter printer) {
        BigDecimal totalBuyInTaxCurrency = StreamEx.of(gain.getBuys())
                .map(buy -> processTradeEntry(buy, rateProvider, printer))
                .reduce(ZERO, BigDecimal::add);

        BigDecimal totalSellInTaxCurrency = processTradeEntry(gain.getSell(), rateProvider, printer);
        BigDecimal totalInTaxCurrency = totalBuyInTaxCurrency.add(totalSellInTaxCurrency);

        printer.printRecord(createRecord("TOTAL", emptyFields(11), totalInTaxCurrency));
        printer.printRecord();

        return totalInTaxCurrency;
    }

    @SneakyThrows
    private BigDecimal processTradeEntry(Trade trade, RateProvider rateProvider, CSVPrinter printer) {
        BigDecimal transaction = trade.getPricePerShare().multiply(new BigDecimal(trade.getQuantity())).negate();
        BigDecimal transactionRate = rateProvider.getRateByDateAndCurrency(trade.getTime().atZone(taxZone).toLocalDate(), trade.getCurrency());
        BigDecimal transactionInTaxCurrency = transaction.multiply(transactionRate);

        BigDecimal feeRate = rateProvider.getRateByDateAndCurrency(trade.getTime().atZone(taxZone).toLocalDate(), trade.getFeeCurrency());
        BigDecimal feeInTaxCurrency = trade.getFee().multiply(feeRate);

        BigDecimal totalInTaxCurrency = transactionInTaxCurrency.add(feeInTaxCurrency);

        printer.printRecord(
                trade.getTime().atZone(taxZone),
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
