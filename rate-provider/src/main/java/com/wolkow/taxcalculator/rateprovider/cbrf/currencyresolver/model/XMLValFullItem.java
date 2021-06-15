package com.wolkow.taxcalculator.rateprovider.cbrf.currencyresolver.model;

import lombok.Data;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

@Data
public class XMLValFullItem {

    @Attribute(name = "ID")
    private String id;

    @Element(name = "Name")
    private String name;

    @Element(name = "EngName")
    private String engName;

    @Element(name = "Nominal")
    private String nominal;

    @Element(name = "ParentCode")
    private String parentCode;

    @Element(name = "ISO_Num_Code", required = false)
    private Integer isoNumCode;

    @Element(name = "ISO_Char_Code", required = false)
    private String isoCharCode;
}
