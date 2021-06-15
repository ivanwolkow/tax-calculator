package com.wolkow.taxcalculator.rateprovider.factories;

import com.wolkow.taxcalculator.rateprovider.RateProvider;
import com.wolkow.taxcalculator.rateprovider.cbrf.CbrRateProvider;
import com.wolkow.taxcalculator.rateprovider.enums.RateProviderType;
import org.apache.commons.lang3.StringUtils;

public class RateProviderFactory {

    private final static String CBR_RATE_PROVIDER_HOST = "http://www.cbr.ru";

    public static RateProvider createRateProvider(RateProviderType providerType, String baseCurrency) {
        if (providerType == RateProviderType.CBR_RATE_PROVIDER) {
            if (!StringUtils.equalsIgnoreCase(baseCurrency, "rub")) {
                throw new RuntimeException("cbr rate provider supports only RUB tax currency");
            }
            return new CbrRateProvider(CBR_RATE_PROVIDER_HOST);
        }
        throw new RuntimeException("Rate provider not supported");
    }
}
