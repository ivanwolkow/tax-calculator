package com.wolkow.taxcalculator.dividend.divprovider;

import com.wolkow.taxcalculator.dividend.model.Dividend;
import com.wolkow.taxcalculator.dividend.properties.ApplicationProperties;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.function.Function.identity;

public class InteractiveBrokersDivProvider implements DivProvider {

    private static final DateTimeFormatter IB_DIV_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ApplicationProperties properties;

    public InteractiveBrokersDivProvider(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<Dividend> readDivs() {
        return StreamEx.of(FileUtils.listFiles(properties.getReportDir(), new String[]{"csv"}, true))
                .map(this::readTradesFromFile)
                .flatCollection(identity())
                .toList();
    }

    @SneakyThrows
    private List<Dividend> readTradesFromFile(File reportFile) {
        CSVParser parser = new CSVParser(new FileReader(reportFile), CSVFormat.DEFAULT.withDelimiter(','));
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
