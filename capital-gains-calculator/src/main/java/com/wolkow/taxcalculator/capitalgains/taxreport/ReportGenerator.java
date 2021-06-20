package com.wolkow.taxcalculator.capitalgains.taxreport;

import com.wolkow.taxcalculator.capitalgains.model.Gain;

import java.util.List;

/**
 * Contract for generating tax report based on list of {@link com.wolkow.taxcalculator.capitalgains.model.Gain gains}
 */
public interface ReportGenerator {

    void generateReport(Appendable appendable, List<Gain> gains);
}
