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
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.gpx2web.binding.kml.AbstractObjectType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractObjectType", propOrder = {
    "objectSimpleExtensionGroups"
})
@XmlSeeAlso({
    PairTypeImpl.class,
    OrientationTypeImpl.class,
    AbstractTimePrimitiveTypeImpl.class,
    ScaleTypeImpl.class,
    RegionTypeImpl.class,
    ItemIconTypeImpl.class,
    AliasTypeImpl.class,
    DataTypeImpl.class,
    SchemaDataTypeImpl.class,
    AbstractGeometryTypeImpl.class,
    ImagePyramidTypeImpl.class,
    ResourceMapTypeImpl.class,
    AbstractSubStyleTypeImpl.class,
    AbstractViewTypeImpl.class,
    AbstractStyleSelectorTypeImpl.class,
    ViewVolumeTypeImpl.class,
    LodTypeImpl.class,
    LocationTypeImpl.class,
    AbstractFeatureTypeImpl.class,
    AbstractLatLonBoxTypeImpl.class,
    BasicLinkTypeImpl.class
})
public abstract class AbstractObjectTypeImpl implements AbstractObjectType
{

    @XmlElement(name = "ObjectSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> objectSimpleExtensionGroups;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String targetId;

    public List<Object> getObjectSimpleExtensionGroups() {
        if (objectSimpleExtensionGroups == null) {
            objectSimpleExtensionGroups = new ArrayList<Object>();
        }
        return this.objectSimpleExtensionGroups;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String value) {
        this.targetId = value;
    }

}