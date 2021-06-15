package com.wolkow.taxcalculator.rateprovider.cbrf.model;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CbrRateRecord {

    @Attribute(name = "Id")
    private String id;

    @Attribute(name = "Date")
    private LocalDate date;

    @Element(name = "Value")
    private BigDecimal value;

    @Element(name = "Nominal")
    private Long nominal;
}
