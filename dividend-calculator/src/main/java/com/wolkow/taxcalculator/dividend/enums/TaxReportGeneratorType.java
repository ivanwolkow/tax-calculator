package com.wolkow.taxcalculator.dividend.enums;

import one.util.streamex.StreamEx;

public enum TaxReportGeneratorType {
    DEFAULT_TAX_REPORT_GENERATOR("default"),
    ;

    private String shortName;

    TaxReportGeneratorType(String shortName) {
        this.shortName = shortName;
    }

    public static TaxReportGeneratorType findByShortName(String shortName) {
        return StreamEx.of(TaxReportGeneratorType.values())
                .findFirst(tp -> tp.getShortName().equals(shortName))
                .orElseThrow();
    }

    public String getShortName() {
        return shortName;
    }
}
