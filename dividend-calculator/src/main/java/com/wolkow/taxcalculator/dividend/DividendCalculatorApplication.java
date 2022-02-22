package com.wolkow.taxcalculator.dividend;

import com.wolkow.taxcalculator.dividend.divprovider.DivProvider;
import com.wolkow.taxcalculator.dividend.divprovider.DivProviders;
import com.wolkow.taxcalculator.dividend.properties.ApplicationProperties;
import com.wolkow.taxcalculator.dividend.taxreport.ReportGenerator;
import com.wolkow.taxcalculator.dividend.taxreport.ReportGenerators;
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

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class DividendCalculatorApplication {

    @SneakyThrows
    public static void main(String[] args) {
        ApplicationProperties props = properties(args);

        log.info("Searching for csv files in {} ...", props.getReportDir());
        var sources = StreamEx.of(FileUtils.listFiles(props.getReportDir(), new String[]{"csv"}, true))
                .peek(file -> log.info("Found: {}", file.getName()))
                .map(DividendCalculatorApplication::createReportReader)
                .toArray(Reader.class);

        var outputFile = props.getOutputFile();
        outputFile.getParentFile().mkdirs();
        FileWriter destination = new FileWriter(outputFile, UTF_8);

        new DividendCalculator(props.getDivProvider(), props.getReportGenerator())
                .generateReport(destination, props.getYear(), sources);

        log.info("Dividend report {} has been successfully generated\n", props.getOutputFile());
    }

    @SneakyThrows
    private static ApplicationProperties properties(String... args) {
        Options options = new Options();
        options.addOption("y", "year", true, "Financial year (default = previous year)");
        options.addOption("i", "in", true, "Directory to be recursively searched for dividend reports (default = ./)");
        options.addOption("o", "out", true, "Name of the csv file this calculator will print result to (default = ./dividend-tax-report.csv)");
        options.addOption("tr", "tax-rate", true, "Tax rate in country of residence (default = 13)");
        options.addOption("tc", "tax-currency", true, "Main currency of the country of residence (default = RUB)");
        options.addOption("dp", "div-provider", true, "Dividend provider (default = ib)");
        options.addOption("rg", "report-generator", true, "Tax report generator (default = csv)");
        options.addOption("h", "help", false, "This help");

        CommandLine p = new DefaultParser().parse(options, args);

        if (p.hasOption("h")) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("dividend-calculator", options);
            System.exit(0);
        }

        Integer year = Integer.valueOf(p.getOptionValue("y", String.valueOf(LocalDate.now().getYear() - 1)));
        String taxCurrency = p.getOptionValue("tc", "RUB");
        BigDecimal taxRate = new BigDecimal(p.getOptionValue("tr", "13.00"));
        RateProvider rateProvider = RateProviders.getByBaseCurrency(taxCurrency);
        DivProvider divProvider = DivProviders.getByName(p.getOptionValue("dp", "ib"));
        ReportGenerator reportGenerator = ReportGenerators.createByName(p.getOptionValue("rg", "csv"), rateProvider, taxRate);

        return ApplicationProperties.builder()
                .reportDir(new File(p.getOptionValue("i", "./")))
                .outputFile(new File(p.getOptionValue("o", String.format("./dividend-tax-report-%d.csv", year))))
                .year(year)
                .taxRate(taxRate)
                .taxCurrency(taxCurrency)
                .divProvider(divProvider)
                .rateProvider(rateProvider)
                .reportGenerator(reportGenerator)
                .build();
    }

    @SneakyThrows
    private static Reader createReportReader(File file) {
        return new FileReader(file);
    }

}
