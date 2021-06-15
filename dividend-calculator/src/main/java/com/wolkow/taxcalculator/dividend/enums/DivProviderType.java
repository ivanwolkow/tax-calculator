package com.wolkow.taxcalculator.dividend.enums;

import one.util.streamex.StreamEx;

public enum DivProviderType {
    INTERACTIVE_BROKERS("ib"),
    ;

    private String shortName;

    DivProviderType(String shortName) {
        this.shortName = shortName;
    }

    public static DivProviderType findByShortName(String shortName) {
        return StreamEx.of(DivProviderType.values())
                .findFirst(tp -> tp.getShortName().equals(shortName))
                .orElseThrow();
    }

    public String getShortName() {
        return shortName;
    }
}
