package com.wolkow.taxcalculator.capitalgains.tradeprovider;

import com.wolkow.taxcalculator.capitalgains.model.Trade;

import java.io.Reader;
import java.util.Collection;

/**
 * Contract for fetching information about user transactions (buys and sells)
 * Implementation must provide information not only about transactions during tax year,
 * but about all the trades: it is required for correct assets accounting and tax calculation
 */
public interface TradeProvider {

    Collection<Trade> readTrades(Reader... readers);

    String getName();

}
