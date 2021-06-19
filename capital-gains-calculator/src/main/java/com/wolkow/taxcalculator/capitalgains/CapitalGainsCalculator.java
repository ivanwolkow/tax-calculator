package com.wolkow.taxcalculator.capitalgains;

import com.google.common.base.Preconditions;
import com.wolkow.taxcalculator.capitalgains.model.Gain;
import com.wolkow.taxcalculator.capitalgains.model.Trade;
import com.wolkow.taxcalculator.capitalgains.taxreport.TaxReportGenerator;
import com.wolkow.taxcalculator.capitalgains.tradeprovider.TradeProvider;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.io.Reader;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
public class CapitalGainsCalculator {

    private final TradeProvider tradeProvider;
    private final TaxReportGenerator reportGenerator;

    public CapitalGainsCalculator(TradeProvider tradeProvider,
                                  TaxReportGenerator reportGenerator) {

        this.tradeProvider = tradeProvider;
        this.reportGenerator = reportGenerator;
    }

    public void calculateTaxReport(Appendable destination, ZoneId taxZone, Integer taxYear, Reader... sources) {
        log.info("Collecting buys and sells...");
        Collection<Trade> tradeList = tradeProvider.readTrades(sources);

        Map<String, LinkedList<Trade>> buysByTicker = StreamEx.of(tradeList)
                .filter(trade -> trade.getQuantity() > 0)
                .sorted(Comparator.comparing(Trade::getTime))
                .groupingBy(Trade::getTicker, Collectors.toCollection(LinkedList::new));

        buysByTicker.forEach((ticker, buys) -> log.info("{}: {} buys", ticker, buys.size()));

        List<Gain> gains = StreamEx.of(tradeList)
                .filter(trade -> trade.getQuantity() < 0)
                .filter(sell -> sell.getTime().atZone(taxZone).getYear() <= taxYear)
                .sorted(Comparator.comparing(Trade::getTime))
                .map(sell -> mapToGain(sell, buysByTicker.get(sell.getTicker())))
                .filter(gain -> gain.getSell().getTime().atZone(taxZone).getYear() == taxYear)
                .toList();

        log.info("Found {} sells in year {}. Lot-Matching result:", gains.size(), taxYear);
        gains.forEach(gain -> log.info(gain.toString()));

        reportGenerator.generateReport(destination, gains);
    }

    private Gain mapToGain(Trade sell, LinkedList<Trade> buys) {
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
}
