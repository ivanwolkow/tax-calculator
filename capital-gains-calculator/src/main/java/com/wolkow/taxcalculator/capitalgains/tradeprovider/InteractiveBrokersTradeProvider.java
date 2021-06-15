package com.wolkow.taxcalculator.capitalgains.tradeprovider;

import com.wolkow.taxcalculator.capitalgains.model.Trade;
import com.wolkow.taxcalculator.capitalgains.properties.ApplicationProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.function.Function.identity;

/**
 * Implementation of {@link TradeProvider} that is able to read Interactive Brokers activity reports
 * In order to use, download activity reports containing all operations since the brokerage account opening
 */
@Slf4j
public class InteractiveBrokersTradeProvider implements TradeProvider {

    private static final String IB_FEE_CURRENCY = "USD";
    private static final DateTimeFormatter IB_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss")
            .withZone(ZoneId.of("-5"));

    private final ApplicationProperties properties;

    public InteractiveBrokersTradeProvider(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<Trade> readTrades() {
        return StreamEx.of(FileUtils.listFiles(properties.getReportDir(), new String[]{"csv"}, true))
                .map(this::readTradesFromFile)
                .flatCollection(identity())
                .toList();
    }

    @SneakyThrows
    private List<Trade> readTradesFromFile(File reportFile) {
        CSVParser parser = new CSVParser(new FileReader(reportFile), CSVFormat.DEFAULT.withDelimiter(','));
        return StreamEx.of(parser.iterator())
                .filter(record -> "trades".equalsIgnoreCase(record.get(0)))
                .filter(record -> "data".equalsIgnoreCase(record.get(1)))
                .filter(record -> "order".equalsIgnoreCase(record.get(2)))
                .map(this::tradeFromRecord)
                .toList();
    }

    private Trade tradeFromRecord(CSVRecord record) {
        return Trade.builder()
                .assetCategory(record.get(3))
                .currency(record.get(4))
                .ticker(record.get(5))
                .time(ZonedDateTime.parse(record.get(6), IB_DATETIME_FORMATTER)
                        .withZoneSameInstant(properties.getTaxTimeZone())
                        .toLocalDateTime())
                .quantity(Integer.parseInt(record.get(7)))
                .pricePerShare(new BigDecimal(record.get(8)))
                .fee(new BigDecimal(record.get(11)))
                .feeCurrency(IB_FEE_CURRENCY)
                .build();
    }


}
