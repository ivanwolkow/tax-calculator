package com.wolkow.taxcalculator.capitalgains.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * When you decide to sell a portion of your holdings, the FIFO principle applies:
 * A sale of stock will be allocated to the shares you bought earliest.
 */
@Getter
@RequiredArgsConstructor
public class Gain {

    private final String ticker;
    private final List<Trade> buys;
    private final Trade sell;

    @Override
    public String toString() {
        return "Gain{" +
                "ticker='" + ticker + '\'' +
                ", buys=" + buys +
                ", sell=" + sell +
                '}';
    }
}
