//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.09.22 at 04:04:36 PM CEST 
//


package org.gpx2web.binding.kml.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;
import org.gpx2web.binding.kml.AddressLine;
import org.gpx2web.binding.kml.MailStopType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MailStopType", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", propOrder = {
    "addressLines",
    "mailStopName",
    "mailStopNumber",
    "anies"
})
public class MailStopTypeImpl
    implements MailStopType
{

    @XmlElement(name = "AddressLine", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = AddressLineImpl.class)
    protected List<AddressLine> addressLines;
    @XmlElement(name = "MailStopName", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = MailStopTypeImpl.MailStopNameImpl.class)
    protected MailStopTypeImpl.MailStopNameImpl mailStopName;
    @XmlElement(name = "MailStopNumber", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = MailStopTypeImpl.MailStopNumberImpl.class)
    protected MailStopTypeImpl.MailStopNumberImpl mailStopNumber;
    @XmlAnyElement(lax = true)
    protected List<Object> anies;
    @XmlAttribute(name = "Type")
    @XmlSchemaType(name = "anySimpleType")
    protected String type;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    public List<AddressLine> getAddressLines() {
        if (addressLines == null) {
            addressLines = new ArrayList<AddressLine>();
        }
        return this.addressLines;
    }

    public MailStopType.MailStopName getMailStopName() {
        return mailStopName;
    }

    public void setMailStopName(MailStopType.MailStopName value) {
        this.mailStopName = ((MailStopTypeImpl.MailStopNameImpl) value);
    }

    public MailStopType.MailStopNumber getMailStopNumber() {
        return mailStopNumber;
    }

    public void setMailStopNumber(MailStopType.MailStopNumber value) {
        this.mailStopNumber = ((MailStopTypeImpl.MailStopNumberImpl) value);
    }

    public List<Object> getAnies() {
        if (anies == null) {
            anies = new ArrayList<Object>();
        }
        return this.anies;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "content"
    })
    public static class MailStopNameImpl
        implements MailStopType.MailStopName
    {

        @XmlValue
        protected String content;
        @XmlAttribute(name = "Type")
        @XmlSchemaType(name = "anySimpleType")
        protected String type;
        @XmlAttribute(name = "Code")
        @XmlSchemaType(name = "anySimpleType")
        protected String code;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();

        public String getContent() {
            return content;
        }

        public void setContent(String value) {
            this.content = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String value) {
            this.type = value;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String value) {
            this.code = value;
        }

        /**
         * Gets a map that contains attributes that aren't bound to any typed property on this class.
         * 
         * <p>
         * the map is keyed by the name of the attribute and 
         * the value is the string value of the attribute.
         * 
         * the map returned by this method is live, and you can add new attribute
         * by updating the map directly. Because of this design, there's no setter.
         * 
         * 
         * @return
         *     always non-null
         */
        public Map<QName, String> getOtherAttributes() {
            return otherAttributes;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "content"
    })
    public static class MailStopNumberImpl
        implements MailStopType.MailStopNumber
    {

        @XmlValue
        protected String content;
        @XmlAttribute(name = "NameNumberSeparator")
        @XmlSchemaType(name = "anySimpleType")
        protected String nameNumberSeparator;
        @XmlAttribute(name = "Code")
        @XmlSchemaType(name = "anySimpleType")
        protected String code;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();

        public String getContent() {
            return content;
        }

        public void setContent(String value) {
            this.content = value;
        }

        public String getNameNumberSeparator() {
            return nameNumberSeparator;
        }

        public void setNameNumberSeparator(String value) {
            this.nameNumberSeparator = value;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String value) {
            this.code = value;
        }

        /**
         * Gets a map that contains attributes that aren't bound to any typed property on this class.
         * 
         * <p>
         * the map is keyed by the name of the attribute and 
         * the value is the string value of the attribute.
         * 
         * the map returned by this method is live, and you can add new attribute
         * by updating the map directly. Because of this design, there's no setter.
         * 
         * 
         * @return
         *     always non-null
         */
        public Map<QName, String> getOtherAttributes() {
            return otherAttributes;
        }

    }

}