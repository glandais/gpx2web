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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.gpx2web.binding.kml.DataType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataType", propOrder = {
    "displayName",
    "value",
    "dataExtensions"
})
public class DataTypeImpl
    extends AbstractObjectTypeImpl
    implements DataType
{

    protected String displayName;
    @XmlElement(required = true)
    protected String value;
    @XmlElement(name = "DataExtension")
    protected List<Object> dataExtensions;
    @XmlAttribute
    protected String name;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String value) {
        this.displayName = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Object> getDataExtensions() {
        if (dataExtensions == null) {
            dataExtensions = new ArrayList<Object>();
        }
        return this.dataExtensions;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

}