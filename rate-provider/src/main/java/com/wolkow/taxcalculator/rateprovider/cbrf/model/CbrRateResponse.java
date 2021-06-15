package com.wolkow.taxcalculator.rateprovider.cbrf.model;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.time.LocalDate;
import java.util.List;

@Root(name = "ValCurs")
@Data
public class CbrRateResponse {

    @Attribute(name = "ID")
    private String id;

    @Attribute(name = "DateRange1")
    private LocalDate fromDate;

    @Attribute(name = "DateRange2")
    private LocalDate toDate;

    @Attribute(name = "name")
    private String name;

    @ElementList(entry = "Record", inline = true)
    private List<CbrRateRecord> records;
}
