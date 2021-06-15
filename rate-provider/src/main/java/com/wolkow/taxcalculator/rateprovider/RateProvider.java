package com.wolkow.taxcalculator.rateprovider;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contract for fetching a currency conversion rate
 */
public interface RateProvider {

    BigDecimal getRateByDateAndCurrency(LocalDate date, String currencyCode);
}
