package com.wolkow.taxcalculator.rateprovider.cbrf.common;

import org.simpleframework.xml.transform.Transform;

import java.math.BigDecimal;

public class CbrBigDecimalTransform implements Transform<BigDecimal> {

    @Override
    public BigDecimal read(String value) throws Exception {
        return new BigDecimal(value.replaceAll(",", "."));
    }

    @Override
    public String write(BigDecimal value) throws Exception {
        return value.toString().replaceAll("\\.", ",");
    }
}
