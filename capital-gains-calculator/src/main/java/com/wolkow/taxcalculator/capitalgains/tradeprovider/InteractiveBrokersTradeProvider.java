package com.wolkow.taxcalculator.capitalgains.tradeprovider;

import com.wolkow.taxcalculator.capitalgains.model.Trade;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.Reader;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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

    @Override
    public String getName() {
        return "ib";
    }

    @Override
    public List<Trade> readTrades(Reader... reportReaders) {
        return StreamEx.of(reportReaders)
                .map(this::readTradesFromReader)
                .flatCollection(identity())
                .toList();
    }

    @SneakyThrows
    private List<Trade> readTradesFromReader(Reader reader) {
        CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withDelimiter(','));
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
                .time(OffsetDateTime.parse(record.get(6), IB_DATETIME_FORMATTER).toInstant())
                .quantity(Integer.parseInt(record.get(7)))
                .pricePerShare(new BigDecimal(record.get(8)))
                .fee(new BigDecimal(record.get(11)))
                .feeCurrency(IB_FEE_CURRENCY)
                .build();
    }


}
