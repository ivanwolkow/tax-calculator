package com.wolkow.taxcalculator.rateprovider;

import com.google.common.collect.ImmutableList;
import com.wolkow.taxcalculator.rateprovider.cbrf.CbrRateProvider;
import one.util.streamex.StreamEx;

import java.util.Map;
import java.util.Objects;

import static java.util.function.Function.identity;

public class RateProviders {

    private final static Map<String, RateProvider> rateProviderByCurrency = StreamEx.of(ImmutableList.<RateProvider>builder()
            .add(new CbrRateProvider("http://www.cbr.ru"))
            .build())
            .toMap(rateProvider -> rateProvider.getBaseCurrency().toUpperCase(), identity());

    public static RateProvider getByBaseCurrency(String baseCurrency) {
        return Objects.requireNonNull(rateProviderByCurrency.get(baseCurrency.toUpperCase()),
                "Unable to find rate provider for currency " + baseCurrency);
    }
}
