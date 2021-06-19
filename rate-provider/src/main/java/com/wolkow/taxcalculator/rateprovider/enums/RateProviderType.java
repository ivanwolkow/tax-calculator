package com.wolkow.taxcalculator.rateprovider.enums;

import one.util.streamex.StreamEx;

import java.util.Map;
import java.util.Objects;

import static java.util.function.Function.identity;

public enum RateProviderType {
    CBR_RATE_PROVIDER("RUB"),
    ;

    private String baseCurrency;

    private static Map<String, RateProviderType> providerByCurrency = StreamEx.of(RateProviderType.values())
            .toMap(RateProviderType::getBaseCurrency, identity());

    RateProviderType(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public static RateProviderType resolveByBaseCurrency(String baseCurrency) {
        return Objects.requireNonNull(providerByCurrency.get(baseCurrency));
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }
}
