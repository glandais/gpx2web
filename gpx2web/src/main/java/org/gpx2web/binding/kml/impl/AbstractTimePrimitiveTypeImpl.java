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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.gpx2web.binding.kml.AbstractObjectType;
import org.gpx2web.binding.kml.AbstractTimePrimitiveType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractTimePrimitiveType", propOrder = {
    "abstractTimePrimitiveSimpleExtensionGroups",
    "abstractTimePrimitiveObjectExtensionGroups"
})
@XmlSeeAlso({
    TimeSpanTypeImpl.class,
    TimeStampTypeImpl.class
})
public abstract class AbstractTimePrimitiveTypeImpl
    extends AbstractObjectTypeImpl
    implements AbstractTimePrimitiveType
{

    @XmlElement(name = "AbstractTimePrimitiveSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> abstractTimePrimitiveSimpleExtensionGroups;
    @XmlElement(name = "AbstractTimePrimitiveObjectExtensionGroup", type = AbstractObjectTypeImpl.class)
    protected List<AbstractObjectType> abstractTimePrimitiveObjectExtensionGroups;

    public List<Object> getAbstractTimePrimitiveSimpleExtensionGroups() {
        if (abstractTimePrimitiveSimpleExtensionGroups == null) {
            abstractTimePrimitiveSimpleExtensionGroups = new ArrayList<Object>();
        }
        return this.abstractTimePrimitiveSimpleExtensionGroups;
    }

    public List<AbstractObjectType> getAbstractTimePrimitiveObjectExtensionGroups() {
        if (abstractTimePrimitiveObjectExtensionGroups == null) {
            abstractTimePrimitiveObjectExtensionGroups = new ArrayList<AbstractObjectType>();
        }
        return this.abstractTimePrimitiveObjectExtensionGroups;
    }

}