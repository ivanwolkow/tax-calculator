package com.wolkow.taxcalculator.dividend.taxreport;

import com.wolkow.taxcalculator.dividend.model.Dividend;

import java.util.List;

public interface DivTaxReportGenerator {

    void generateReport(List<Dividend> dividends);
}
