package com.wolkow.taxcalculator.rateprovider.cbrf.currencyresolver.model;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "Valuta")
@Data
public class XMLValFullResponse {

    @Attribute(name = "name")
    private String name;

    @ElementList(entry = "Item", inline = true)
    private List<XMLValFullItem> items;
}
