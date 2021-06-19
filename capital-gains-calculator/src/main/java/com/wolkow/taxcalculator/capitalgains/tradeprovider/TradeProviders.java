package com.wolkow.taxcalculator.capitalgains.tradeprovider;

import com.google.common.collect.ImmutableList;
import one.util.streamex.StreamEx;

import java.util.Map;
import java.util.Objects;

import static java.util.function.Function.identity;

public class TradeProviders {

    private static final Map<String, TradeProvider> providersByName = StreamEx.of(ImmutableList.<TradeProvider>builder()
            .add(new InteractiveBrokersTradeProvider())
            .build())
            .toMap(tradeProvider -> tradeProvider.getName().toLowerCase(), identity());


    public static TradeProvider getByName(String name) {
        return Objects.requireNonNull(providersByName.get(name.toLowerCase()));
    }
}
