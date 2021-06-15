package com.wolkow.taxcalculator.capitalgains;

import com.google.common.base.Preconditions;
import com.wolkow.taxcalculator.capitalgains.enums.TaxReportGeneratorType;
import com.wolkow.taxcalculator.capitalgains.enums.TradeProviderType;
import com.wolkow.taxcalculator.capitalgains.factories.TaxReportGeneratorFactory;
import com.wolkow.taxcalculator.capitalgains.factories.TradeProviderFactory;
import com.wolkow.taxcalculator.capitalgains.model.Gain;
import com.wolkow.taxcalculator.capitalgains.model.Trade;
import com.wolkow.taxcalculator.capitalgains.properties.ApplicationProperties;
import com.wolkow.taxcalculator.capitalgains.taxreport.TaxReportGenerator;
import com.wolkow.taxcalculator.capitalgains.tradeprovider.TradeProvider;
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
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
public class CapitalGainsCalculatorApplication {

    private final ApplicationProperties properties;
    private final TradeProvider tradeProvider;
    private final TaxReportGenerator taxReportGenerator;

    private Map<String, LinkedList<Trade>> buysByTicker;

    public CapitalGainsCalculatorApplication(ApplicationProperties properties,
                                             TradeProvider tradeProvider,
                                             TaxReportGenerator taxReportGenerator) {
        this.properties = properties;
        this.tradeProvider = tradeProvider;
        this.taxReportGenerator = taxReportGenerator;
    }

    public static void main(String[] args) {
        ApplicationProperties properties = properties(args);
        var rateProvider = RateProviderFactory.createRateProvider(properties.getRateProvider(), properties.getTaxCurrency());
        var tradeProvider = TradeProviderFactory.createTradeProvider(properties);
        var taxReportGenerator = TaxReportGeneratorFactory.createTaxReportGenerator(properties, rateProvider);

        new CapitalGainsCalculatorApplication(properties, tradeProvider, taxReportGenerator).run();
    }

    void run() {
        log.info("Collecting trades...");
        Collection<Trade> tradeList = tradeProvider.readTrades();

        log.info("Grouping buys by ticker and sorting...");
        buysByTicker = StreamEx.of(tradeList)
                .filter(trade -> trade.getQuantity() > 0)
                .sorted(Comparator.comparing(Trade::getTime))
                .groupingBy(Trade::getTicker, Collectors.toCollection(LinkedList::new));
        buysByTicker.forEach((ticker, buys) -> log.info("{}: {} buys", ticker, buys.size()));

        log.info("Mapping sells to buys...");
        List<Gain> gains = StreamEx.of(tradeList)
                .filter(trade -> trade.getQuantity() < 0)
                .filter(sell -> sell.getTime().getYear() <= properties.getYear())
                .sorted(Comparator.comparing(Trade::getTime))
                .map(this::mapToGain)
                .filter(gain -> gain.getSell().getTime().getYear() == properties.getYear())
                .toList();
        log.info("Found {} sells in year {}: {}", gains.size(), properties.getYear(), gains);

        taxReportGenerator.generateReport(gains);
    }

    private Gain mapToGain(Trade sell) {
        LinkedList<Trade> buys = buysByTicker.get(sell.getTicker());

        Preconditions.checkArgument(buys.size() > 0);

        List<Trade> gainBuys = new ArrayList<>();
        int sellQuantity = Math.abs(sell.getQuantity());

        while (true) {
            Trade buy = requireNonNull(buys.pollFirst());
            int withdrawQuantity = Math.min(sellQuantity, buy.getQuantity());
            sellQuantity -= withdrawQuantity;

            if (sellQuantity != 0) {
                gainBuys.add(buy);
                continue;
            }

            if (buy.getQuantity() == withdrawQuantity) {
                gainBuys.add(buy);
            } else {
                gainBuys.add(buy.clone(withdrawQuantity));
                buys.addFirst(buy.clone(buy.getQuantity() - withdrawQuantity));
            }
            break;
        }

        return new Gain(sell.getTicker(), gainBuys, sell);
    }

    @SneakyThrows
    private static ApplicationProperties properties(String... args) {
        Options options = new Options();
        options.addOption("y", "year", true, "Financial year (default = previous year)");
        options.addOption("i", "in", true, "Path to directory with broker activity reports (default = ./)");
        options.addOption("o", "out", true, "Name of the csv file this calculator will print result to (default = ./capital-gains-tax-report.csv)");
        options.addOption("tr", "tax-rate", true, "Tax rate in country of residence (default = 13)");
        options.addOption("tc", "tax-currency", true, "Main currency of the country of residence (default = RUB)");
        options.addOption("tz", "tax-tz", true, "Tax timezone (default = +03)");
        options.addOption("tp", "trade-provider", true, "Trade provider (default = ib)");
        options.addOption("rp", "rate-provider", true, "Conversion rate provider (default = cbr)");
        options.addOption("rg", "report-generator", true, "Tax report generator (default = default)");
        options.addOption("h", "help", false, "This help");

        CommandLine p = new DefaultParser().parse(options, args);

        if (p.hasOption("h")) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("capital-gains-calculator ", options);
            System.exit(0);
        }

        return ApplicationProperties.builder()
                .year(Integer.valueOf(p.getOptionValue("y", String.valueOf(LocalDate.now().getYear() - 1))))
                .reportDir(new File(p.getOptionValue("i", "./")))
                .outputFile(new File(p.getOptionValue("o", "./capital-gains-tax-report.csv")))
                .taxRate(new BigDecimal(p.getOptionValue("tr", "13.00")))
                .taxCurrency(p.getOptionValue("tc", "RUB"))
                .taxTimeZone(ZoneId.of(p.getOptionValue("tz", "+03")))
                .tradeProvider(TradeProviderType.findByShortName(p.getOptionValue("tp", "ib")))
                .rateProvider(RateProviderType.findByShortName(p.getOptionValue("rp", "cbr")))
                .taxReportGenerator(TaxReportGeneratorType.findByShortName(p.getOptionValue("rg", "default")))
                .build();

    }

}
