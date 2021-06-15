package com.wolkow.taxcalculator.rateprovider.cbrf.common;

import org.simpleframework.xml.transform.Transform;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CbrLocalDateTransform implements Transform<LocalDate> {

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public LocalDate read(String value) throws Exception {
        return LocalDate.parse(value, formatter);
    }

    @Override
    public String write(LocalDate value) throws Exception {
        return formatter.format(value);
    }
}
