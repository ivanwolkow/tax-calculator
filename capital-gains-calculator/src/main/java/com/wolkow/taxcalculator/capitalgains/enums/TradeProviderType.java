package com.wolkow.taxcalculator.capitalgains.enums;

import one.util.streamex.StreamEx;

public enum TradeProviderType {
    INTERACTIVE_BROKERS("ib"),
    ;

    private String shortName;

    TradeProviderType(String shortName) {
        this.shortName = shortName;
    }

    public static TradeProviderType findByShortName(String shortName) {
        return StreamEx.of(TradeProviderType.values())
                .findFirst(tp -> tp.getShortName().equals(shortName))
                .orElseThrow();
    }

    public String getShortName() {
        return shortName;
    }
}
