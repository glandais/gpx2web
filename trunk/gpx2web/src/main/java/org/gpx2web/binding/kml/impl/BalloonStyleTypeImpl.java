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
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.gpx2web.binding.kml.AbstractObjectType;
import org.gpx2web.binding.kml.BalloonStyleType;
import org.gpx2web.binding.kml.DisplayModeEnumType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BalloonStyleType", propOrder = {
    "bgColor",
    "color",
    "textColor",
    "text",
    "displayMode",
    "balloonStyleSimpleExtensionGroups",
    "balloonStyleObjectExtensionGroups"
})
public class BalloonStyleTypeImpl
    extends AbstractSubStyleTypeImpl
    implements BalloonStyleType
{

    @XmlElement(type = String.class, defaultValue = "ffffffff")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] bgColor;
    @XmlElement(type = String.class, defaultValue = "ffffffff")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] color;
    @XmlElement(type = String.class, defaultValue = "ff000000")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] textColor;
    protected String text;
    @XmlElement(defaultValue = "default")
    protected DisplayModeEnumType displayMode;
    @XmlElement(name = "BalloonStyleSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> balloonStyleSimpleExtensionGroups;
    @XmlElement(name = "BalloonStyleObjectExtensionGroup", type = AbstractObjectTypeImpl.class)
    protected List<AbstractObjectType> balloonStyleObjectExtensionGroups;

    public byte[] getBgColor() {
        return bgColor;
    }

    public void setBgColor(byte[] value) {
        this.bgColor = ((byte[]) value);
    }

    public byte[] getColor() {
        return color;
    }

    public void setColor(byte[] value) {
        this.color = ((byte[]) value);
    }

    public byte[] getTextColor() {
        return textColor;
    }

    public void setTextColor(byte[] value) {
        this.textColor = ((byte[]) value);
    }

    public String getText() {
        return text;
    }

    public void setText(String value) {
        this.text = value;
    }

    public DisplayModeEnumType getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayModeEnumType value) {
        this.displayMode = value;
    }

    public List<Object> getBalloonStyleSimpleExtensionGroups() {
        if (balloonStyleSimpleExtensionGroups == null) {
            balloonStyleSimpleExtensionGroups = new ArrayList<Object>();
        }
        return this.balloonStyleSimpleExtensionGroups;
    }

    public List<AbstractObjectType> getBalloonStyleObjectExtensionGroups() {
        if (balloonStyleObjectExtensionGroups == null) {
            balloonStyleObjectExtensionGroups = new ArrayList<AbstractObjectType>();
        }
        return this.balloonStyleObjectExtensionGroups;
    }

}