package com.wolkow.taxcalculator.rateprovider.enums;

import one.util.streamex.StreamEx;

public enum RateProviderType {
    CBR_RATE_PROVIDER("cbr"),
    ;

    private String shortName;

    RateProviderType(String shortName) {
        this.shortName = shortName;
    }

    public static RateProviderType findByShortName(String shortName) {
        return StreamEx.of(RateProviderType.values())
                .findFirst(tp -> tp.getShortName().equals(shortName))
                .orElseThrow();
    }

    public String getShortName() {
        return shortName;
    }
}
