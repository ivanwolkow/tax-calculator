package com.wolkow.taxcalculator.dividend;

import com.wolkow.taxcalculator.dividend.divprovider.DivProvider;
import com.wolkow.taxcalculator.dividend.enums.DivProviderType;
import com.wolkow.taxcalculator.dividend.enums.TaxReportGeneratorType;
import com.wolkow.taxcalculator.dividend.factories.DivProviderFactory;
import com.wolkow.taxcalculator.dividend.factories.TaxReportGeneratorFactory;
import com.wolkow.taxcalculator.dividend.model.Dividend;
import com.wolkow.taxcalculator.dividend.properties.ApplicationProperties;
import com.wolkow.taxcalculator.dividend.taxreport.DivTaxReportGenerator;
import com.wolkow.taxcalculator.rateprovider.enums.RateProviderType;
import com.wolkow.taxcalculator.rateprovider.factories.RateProviderFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
public class DividendCalculatorApplication {

    private final ApplicationProperties properties;
    private final DivProvider divProvider;
    private final DivTaxReportGenerator reportGenerator;

    public DividendCalculatorApplication(ApplicationProperties properties, DivProvider divProvider, DivTaxReportGenerator reportGenerator) {
        this.properties = properties;
        this.divProvider = divProvider;
        this.reportGenerator = reportGenerator;
    }

    public static void main(String[] args) {
        ApplicationProperties properties = properties(args);
        var divProvider = DivProviderFactory.createDivProvider(properties);
        var rateProvider = RateProviderFactory.createRateProvider(properties.getRateProvider(), properties.getTaxCurrency());
        var reportGenerator = TaxReportGeneratorFactory.createTaxReportGenerator(properties, rateProvider);

        new DividendCalculatorApplication(properties, divProvider, reportGenerator).run();
    }

    public void run() {
        log.info("Searching for dividend accruals...");

        List<Dividend> dividends = StreamEx.of(divProvider.readDivs())
                .filterBy(div -> div.getDate().getYear(), properties.getYear())
                .sortedBy(Dividend::getDate)
                .toList();

        log.info("Found {} dividends in year {}", dividends.size(), properties.getYear());
        dividends.forEach(div -> log.info(div.toString()));

        reportGenerator.generateReport(dividends);
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
        options.addOption("rp", "rate-provider", true, "Conversion rate provider (default = cbr)");
        options.addOption("rg", "report-generator", true, "Tax report generator (default = default)");
        options.addOption("h", "help", false, "This help");

        CommandLine p = new DefaultParser().parse(options, args);

        if (p.hasOption("h")) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("dividend-calculator", options);
            System.exit(0);
        }

        var year = Integer.valueOf(p.getOptionValue("y", String.valueOf(LocalDate.now().getYear() - 1)));

        return ApplicationProperties.builder()
                .year(year)
                .reportDir(new File(p.getOptionValue("i", "./")))
                .outputFile(new File(p.getOptionValue("o", String.format("./dividend-tax-report-%d.csv", year))))
                .taxRate(new BigDecimal(p.getOptionValue("tr", "13.00")))
                .taxCurrency(p.getOptionValue("tc", "RUB"))
                .divProvider(DivProviderType.findByShortName(p.getOptionValue("dp", "ib")))
                .rateProvider(RateProviderType.findByShortName(p.getOptionValue("rp", "cbr")))
                .taxReportGenerator(TaxReportGeneratorType.findByShortName(p.getOptionValue("rg", "default")))
                .build();

    }

}
