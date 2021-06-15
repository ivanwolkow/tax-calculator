package com.wolkow.taxcalculator.capitalgains.factories;

import com.wolkow.taxcalculator.capitalgains.enums.TradeProviderType;
import com.wolkow.taxcalculator.capitalgains.properties.ApplicationProperties;
import com.wolkow.taxcalculator.capitalgains.tradeprovider.InteractiveBrokersTradeProvider;
import com.wolkow.taxcalculator.capitalgains.tradeprovider.TradeProvider;

public class TradeProviderFactory {

    public static TradeProvider createTradeProvider(ApplicationProperties properties) {
        if (properties.getTradeProvider() == TradeProviderType.INTERACTIVE_BROKERS) {
            return new InteractiveBrokersTradeProvider(properties);
        }
        throw new RuntimeException("Trade provider not supported");
    }
}
