package com.wolkow.taxcalculator.rateprovider.cbrf.currencyresolver;

/**
 * Internal contract for resolving ISO 4217 currency code to internal code used in
 * {@link CbrCurrencyResolver}
 */
public interface CurrencyResolver {

    String resolveCodeByName(String name);
}
