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
import org.gpx2web.binding.kml.TimeStampType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimeStampType", propOrder = {
    "when",
    "timeStampSimpleExtensionGroups",
    "timeStampObjectExtensionGroups"
})
public class TimeStampTypeImpl
    extends AbstractTimePrimitiveTypeImpl
    implements TimeStampType
{

    protected String when;
    @XmlElement(name = "TimeStampSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> timeStampSimpleExtensionGroups;
    @XmlElement(name = "TimeStampObjectExtensionGroup", type = AbstractObjectTypeImpl.class)
    protected List<AbstractObjectType> timeStampObjectExtensionGroups;

    public String getWhen() {
        return when;
    }

    public void setWhen(String value) {
        this.when = value;
    }

    public List<Object> getTimeStampSimpleExtensionGroups() {
        if (timeStampSimpleExtensionGroups == null) {
            timeStampSimpleExtensionGroups = new ArrayList<Object>();
        }
        return this.timeStampSimpleExtensionGroups;
    }

    public List<AbstractObjectType> getTimeStampObjectExtensionGroups() {
        if (timeStampObjectExtensionGroups == null) {
            timeStampObjectExtensionGroups = new ArrayList<AbstractObjectType>();
        }
        return this.timeStampObjectExtensionGroups;
    }

}