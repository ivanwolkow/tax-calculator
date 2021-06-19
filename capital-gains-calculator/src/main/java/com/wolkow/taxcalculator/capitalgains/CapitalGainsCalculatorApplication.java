package com.wolkow.taxcalculator.capitalgains;

import com.wolkow.taxcalculator.capitalgains.properties.ApplicationProperties;
import com.wolkow.taxcalculator.capitalgains.taxreport.TaxReportGenerator;
import com.wolkow.taxcalculator.capitalgains.taxreport.TaxReportGenerators;
import com.wolkow.taxcalculator.capitalgains.tradeprovider.TradeProvider;
import com.wolkow.taxcalculator.capitalgains.tradeprovider.TradeProviders;
import com.wolkow.taxcalculator.rateprovider.RateProvider;
import com.wolkow.taxcalculator.rateprovider.RateProviders;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class CapitalGainsCalculatorApplication {

    @SneakyThrows
    public static void main(String[] args) {
        ApplicationProperties props = properties(args);

        var sources = StreamEx.of(FileUtils.listFiles(props.getReportDir(), new String[]{"csv"}, true))
                .map(CapitalGainsCalculatorApplication::createReportReader)
                .toArray(Reader.class);

        var outputFile = props.getOutputFile();
        outputFile.getParentFile().mkdirs();
        FileWriter destination = new FileWriter(outputFile, UTF_8);

        new CapitalGainsCalculator(props.getTradeProvider(), props.getTaxReportGenerator())
                .calculateTaxReport(destination, props.getTaxTimeZone(), props.getYear(), sources);
    }

    @SneakyThrows
    private static ApplicationProperties properties(String... args) {
        Options options = new Options();
        options.addOption("y", "year", true, "Financial year (default = previous year)");
        options.addOption("i", "in", true, "Directory to be recursively searched for activity reports (default = ./)");
        options.addOption("o", "out", true, "Name of the csv file this calculator will print result to (default = ./capital-gains-tax-report.csv)");
        options.addOption("tr", "tax-rate", true, "Tax rate in country of residence (default = 13)");
        options.addOption("tc", "tax-currency", true, "Main currency of the country of residence (default = RUB)");
        options.addOption("tz", "tax-tz", true, "Tax timezone (default = +03)");
        options.addOption("tp", "trade-provider", true, "Trade provider (default = ib)");
        options.addOption("rg", "report-generator", true, "Tax report generator (default = default)");
        options.addOption("h", "help", false, "This help");

        CommandLine p = new DefaultParser().parse(options, args);

        if (p.hasOption("h")) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("capital-gains-calculator ", options);
            System.exit(0);
        }

        Integer year = Integer.valueOf(p.getOptionValue("y", String.valueOf(LocalDate.now().getYear() - 1)));
        String taxCurrency = p.getOptionValue("tc", "RUB");
        ZoneId taxZone = ZoneId.of(p.getOptionValue("tz", "+03"));
        BigDecimal taxRate = new BigDecimal(p.getOptionValue("tr", "13.00"));
        TradeProvider tradeProvider = TradeProviders.getByName(p.getOptionValue("tp", "ib"));
        RateProvider rateProvider = RateProviders.getByBaseCurrency(taxCurrency);
        TaxReportGenerator reportGenerator = TaxReportGenerators.createByName(p.getOptionValue("rg", "csv"), rateProvider, taxRate, taxZone);

        return ApplicationProperties.builder()
                .reportDir(new File(p.getOptionValue("i", "./")))
                .outputFile(new File(p.getOptionValue("o", String.format("./capital-gains-tax-report-%d.csv", year))))
                .year(year)
                .taxRate(taxRate)
                .taxCurrency(taxCurrency)
                .taxTimeZone(taxZone)
                .tradeProvider(tradeProvider)
                .taxReportGenerator(reportGenerator)
                .build();
    }

    @SneakyThrows
    private static Reader createReportReader(File file) {
        return new FileReader(file);
    }


}
