package com.wolkow.taxcalculator.dividend.taxreport;

import com.wolkow.taxcalculator.dividend.model.Dividend;

import java.util.List;

public interface ReportGenerator {

    void generateReport(Appendable appendable, List<Dividend> dividends);

    String getName();
}
