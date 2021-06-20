package com.wolkow.taxcalculator.api.service;

import com.wolkow.taxcalculator.api.dto.CapitalGainsRequest;
import com.wolkow.taxcalculator.capitalgains.CapitalGainsCalculator;
import com.wolkow.taxcalculator.capitalgains.taxreport.ReportGenerators;
import com.wolkow.taxcalculator.capitalgains.tradeprovider.TradeProviders;
import com.wolkow.taxcalculator.rateprovider.RateProviders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Reader;
import java.io.Writer;

@Service
@RequiredArgsConstructor
@Slf4j
public class CapitalGainsCalculatorService {

    public void generateCapitalGainsReport(Writer destination,
                                           CapitalGainsRequest rq,
                                           Reader... brokerReports) {

        var rateProvider = RateProviders.getByBaseCurrency(rq.getTaxCurrency());
        var tradeProvider = TradeProviders.getByName(rq.getBrokerType());
        var reportGenerator = ReportGenerators.createByName(rq.getReportType(), rateProvider, rq.getTaxRate(), rq.getTaxZone());

        new CapitalGainsCalculator(tradeProvider, reportGenerator)
                .generateReport(destination, rq.getTaxZone(), rq.getTaxYear(), brokerReports);
    }

}
