package com.wolkow.taxcalculator.api.service;

import com.wolkow.taxcalculator.api.dto.DividendRequest;
import com.wolkow.taxcalculator.dividend.DividendCalculator;
import com.wolkow.taxcalculator.dividend.divprovider.DivProviders;
import com.wolkow.taxcalculator.dividend.taxreport.ReportGenerators;
import com.wolkow.taxcalculator.rateprovider.RateProviders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Reader;
import java.io.Writer;

@Service
@Slf4j
@RequiredArgsConstructor
public class DividendCalculatorService {

    public void generateDividendReport(Writer destination,
                                       DividendRequest rq,
                                       Reader... brokerReports) {

        var rateProvider = RateProviders.getByBaseCurrency(rq.getTaxCurrency());
        var divProvider = DivProviders.getByName(rq.getBrokerType());
        var reportGenerator = ReportGenerators.createByName(rq.getReportType(), rateProvider, rq.getTaxRate());

        new DividendCalculator(divProvider, reportGenerator)
                .generateReport(destination, rq.getTaxYear(), brokerReports);
    }
}
