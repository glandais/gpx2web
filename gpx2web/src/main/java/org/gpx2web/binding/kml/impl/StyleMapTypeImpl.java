//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.09.22 at 04:04:36 PM CEST 
//


package org.gpx2web.binding.kml.impl;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.gpx2web.binding.kml.AbstractObjectType;
import org.gpx2web.binding.kml.PairType;
import org.gpx2web.binding.kml.StyleMapType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StyleMapType", propOrder = {
    "pairs",
    "styleMapSimpleExtensionGroups",
    "styleMapObjectExtensionGroups"
})
public class StyleMapTypeImpl
    extends AbstractStyleSelectorTypeImpl
    implements StyleMapType
{

    @XmlElement(name = "Pair", type = PairTypeImpl.class)
    protected List<PairType> pairs;
    @XmlElement(name = "StyleMapSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> styleMapSimpleExtensionGroups;
    @XmlElement(name = "StyleMapObjectExtensionGroup", type = AbstractObjectTypeImpl.class)
    protected List<AbstractObjectType> styleMapObjectExtensionGroups;

    public List<PairType> getPairs() {
        if (pairs == null) {
            pairs = new ArrayList<PairType>();
        }
        return this.pairs;
    }

    public List<Object> getStyleMapSimpleExtensionGroups() {
        if (styleMapSimpleExtensionGroups == null) {
            styleMapSimpleExtensionGroups = new ArrayList<Object>();
        }
        return this.styleMapSimpleExtensionGroups;
    }

    public List<AbstractObjectType> getStyleMapObjectExtensionGroups() {
        if (styleMapObjectExtensionGroups == null) {
            styleMapObjectExtensionGroups = new ArrayList<AbstractObjectType>();
        }
        return this.styleMapObjectExtensionGroups;
    }

}