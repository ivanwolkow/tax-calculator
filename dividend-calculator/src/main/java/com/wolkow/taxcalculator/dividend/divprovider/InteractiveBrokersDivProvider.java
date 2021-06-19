package com.wolkow.taxcalculator.dividend.divprovider;

import com.wolkow.taxcalculator.dividend.model.Dividend;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.function.Function.identity;

public class InteractiveBrokersDivProvider implements DivProvider {

    private static final DateTimeFormatter IB_DIV_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public String getName() {
        return "ib";
    }

    @Override
    public List<Dividend> readDivs(Reader... reportReaders) {
        return StreamEx.of(reportReaders)
                .map(this::readDivsFromReader)
                .flatCollection(identity())
                .toList();
    }

    @SneakyThrows
    private List<Dividend> readDivsFromReader(Reader reportReader) {
        CSVParser parser = new CSVParser(reportReader, CSVFormat.DEFAULT.withDelimiter(','));
        return StreamEx.of(parser.iterator())
                .filter(record -> "DividendDetail".equalsIgnoreCase(record.get(0)))
                .filter(record -> "Data".equalsIgnoreCase(record.get(1)))
                .filter(record -> "Summary".equalsIgnoreCase(record.get(2)))
                .map(this::tradeFromRecord)
                .toList();
    }

    private Dividend tradeFromRecord(CSVRecord record) {
        return Dividend.builder()
                .currency(record.get(3))
                .ticker(record.get(4))
                .country(record.get(6))
                .date(LocalDate.parse(record.get(7), IB_DIV_DATE_FORMATTER))
                .quantity(Integer.parseInt(record.get(9)))
                .gross(new BigDecimal(record.get(12)))
                .withhold(new BigDecimal(record.get(15)))
                .build();
    }
}
