package com.wolkow.taxcalculator.dividend.divprovider;

import com.google.common.collect.ImmutableList;
import one.util.streamex.StreamEx;

import java.util.Map;
import java.util.Objects;

import static java.util.function.Function.identity;

public class DivProviders {

    private static final Map<String, DivProvider> providersByName = StreamEx.of(ImmutableList.<DivProvider>builder()
            .add(new InteractiveBrokersDivProvider())
            .build())
            .toMap(divProvider -> divProvider.getName().toLowerCase(), identity());


    public static DivProvider getByName(String name) {
        return Objects.requireNonNull(providersByName.get(name.toLowerCase()));
    }
}
