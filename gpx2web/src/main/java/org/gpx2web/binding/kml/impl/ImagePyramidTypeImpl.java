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
import org.gpx2web.binding.kml.GridOriginEnumType;
import org.gpx2web.binding.kml.ImagePyramidType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ImagePyramidType", propOrder = {
    "tileSize",
    "maxWidth",
    "maxHeight",
    "gridOrigin",
    "imagePyramidSimpleExtensionGroups",
    "imagePyramidObjectExtensionGroups"
})
public class ImagePyramidTypeImpl
    extends AbstractObjectTypeImpl
    implements ImagePyramidType
{

    @XmlElement(defaultValue = "256")
    protected Integer tileSize;
    @XmlElement(defaultValue = "0")
    protected Integer maxWidth;
    @XmlElement(defaultValue = "0")
    protected Integer maxHeight;
    @XmlElement(defaultValue = "lowerLeft")
    protected GridOriginEnumType gridOrigin;
    @XmlElement(name = "ImagePyramidSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> imagePyramidSimpleExtensionGroups;
    @XmlElement(name = "ImagePyramidObjectExtensionGroup", type = AbstractObjectTypeImpl.class)
    protected List<AbstractObjectType> imagePyramidObjectExtensionGroups;

    public Integer getTileSize() {
        return tileSize;
    }

    public void setTileSize(Integer value) {
        this.tileSize = value;
    }

    public Integer getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(Integer value) {
        this.maxWidth = value;
    }

    public Integer getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(Integer value) {
        this.maxHeight = value;
    }

    public GridOriginEnumType getGridOrigin() {
        return gridOrigin;
    }

    public void setGridOrigin(GridOriginEnumType value) {
        this.gridOrigin = value;
    }

    public List<Object> getImagePyramidSimpleExtensionGroups() {
        if (imagePyramidSimpleExtensionGroups == null) {
            imagePyramidSimpleExtensionGroups = new ArrayList<Object>();
        }
        return this.imagePyramidSimpleExtensionGroups;
    }

    public List<AbstractObjectType> getImagePyramidObjectExtensionGroups() {
        if (imagePyramidObjectExtensionGroups == null) {
            imagePyramidObjectExtensionGroups = new ArrayList<AbstractObjectType>();
        }
        return this.imagePyramidObjectExtensionGroups;
    }

}