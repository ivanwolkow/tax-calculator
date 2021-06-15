package com.wolkow.taxcalculator.dividend.divprovider;

import com.wolkow.taxcalculator.dividend.model.Dividend;

import java.util.List;

public interface DivProvider {

    List<Dividend> readDivs();
}
