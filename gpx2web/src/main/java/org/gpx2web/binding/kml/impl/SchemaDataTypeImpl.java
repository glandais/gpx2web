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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.gpx2web.binding.kml.SchemaDataType;
import org.gpx2web.binding.kml.SimpleData;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SchemaDataType", propOrder = {
    "simpleDatas",
    "schemaDataExtensions"
})
public class SchemaDataTypeImpl
    extends AbstractObjectTypeImpl
    implements SchemaDataType
{

    @XmlElement(name = "SimpleData", type = SimpleDataImpl.class)
    protected List<SimpleData> simpleDatas;
    @XmlElement(name = "SchemaDataExtension")
    protected List<Object> schemaDataExtensions;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    protected String schemaUrl;

    public List<SimpleData> getSimpleDatas() {
        if (simpleDatas == null) {
            simpleDatas = new ArrayList<SimpleData>();
        }
        return this.simpleDatas;
    }

    public List<Object> getSchemaDataExtensions() {
        if (schemaDataExtensions == null) {
            schemaDataExtensions = new ArrayList<Object>();
        }
        return this.schemaDataExtensions;
    }

    public String getSchemaUrl() {
        return schemaUrl;
    }

    public void setSchemaUrl(String value) {
        this.schemaUrl = value;
    }

}