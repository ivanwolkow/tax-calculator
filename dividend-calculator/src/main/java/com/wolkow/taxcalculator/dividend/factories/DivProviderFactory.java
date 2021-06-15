package com.wolkow.taxcalculator.dividend.factories;

import com.wolkow.taxcalculator.dividend.divprovider.DivProvider;
import com.wolkow.taxcalculator.dividend.divprovider.InteractiveBrokersDivProvider;
import com.wolkow.taxcalculator.dividend.enums.DivProviderType;
import com.wolkow.taxcalculator.dividend.properties.ApplicationProperties;

public class DivProviderFactory {

    public static DivProvider createDivProvider(ApplicationProperties properties) {
        if (properties.getDivProvider() == DivProviderType.INTERACTIVE_BROKERS) {
            return new InteractiveBrokersDivProvider(properties);
        }
        throw new RuntimeException("Div provider not supported");
    }
}
