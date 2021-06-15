package com.wolkow.taxcalculator.rateprovider.cbrf.common;

import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CbrXmlMatcher implements Matcher {

    @Override
    public Transform match(Class type) throws Exception {
        if (type == BigDecimal.class) return new CbrBigDecimalTransform();
        if (type == LocalDate.class) return new CbrLocalDateTransform();
        return null;
    }
}
