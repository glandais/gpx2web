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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;
import org.gpx2web.binding.kml.AddressLine;
import org.gpx2web.binding.kml.PostalCode;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "addressLines",
    "postalCodeNumbers",
    "postalCodeNumberExtensions",
    "postTown",
    "anies"
})
@XmlRootElement(name = "PostalCode", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
public class PostalCodeImpl
    implements PostalCode
{

    @XmlElement(name = "AddressLine", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = AddressLineImpl.class)
    protected List<AddressLine> addressLines;
    @XmlElement(name = "PostalCodeNumber", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = PostalCodeImpl.PostalCodeNumberImpl.class)
    protected List<PostalCode.PostalCodeNumber> postalCodeNumbers;
    @XmlElement(name = "PostalCodeNumberExtension", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = PostalCodeImpl.PostalCodeNumberExtensionImpl.class)
    protected List<PostalCode.PostalCodeNumberExtension> postalCodeNumberExtensions;
    @XmlElement(name = "PostTown", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = PostalCodeImpl.PostTownImpl.class)
    protected PostalCodeImpl.PostTownImpl postTown;
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

    public List<PostalCode.PostalCodeNumber> getPostalCodeNumbers() {
        if (postalCodeNumbers == null) {
            postalCodeNumbers = new ArrayList<PostalCode.PostalCodeNumber>();
        }
        return this.postalCodeNumbers;
    }

    public List<PostalCode.PostalCodeNumberExtension> getPostalCodeNumberExtensions() {
        if (postalCodeNumberExtensions == null) {
            postalCodeNumberExtensions = new ArrayList<PostalCode.PostalCodeNumberExtension>();
        }
        return this.postalCodeNumberExtensions;
    }

    public PostalCode.PostTown getPostTown() {
        return postTown;
    }

    public void setPostTown(PostalCode.PostTown value) {
        this.postTown = ((PostalCodeImpl.PostTownImpl) value);
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
    public static class PostalCodeNumberExtensionImpl
        implements PostalCode.PostalCodeNumberExtension
    {

        @XmlValue
        protected String content;
        @XmlAttribute(name = "Type")
        @XmlSchemaType(name = "anySimpleType")
        protected String type;
        @XmlAttribute(name = "NumberExtensionSeparator")
        @XmlSchemaType(name = "anySimpleType")
        protected String numberExtensionSeparator;
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

        public String getNumberExtensionSeparator() {
            return numberExtensionSeparator;
        }

        public void setNumberExtensionSeparator(String value) {
            this.numberExtensionSeparator = value;
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
    public static class PostalCodeNumberImpl
        implements PostalCode.PostalCodeNumber
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
        "addressLines",
        "postTownNames",
        "postTownSuffix"
    })
    public static class PostTownImpl
        implements PostalCode.PostTown
    {

        @XmlElement(name = "AddressLine", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = AddressLineImpl.class)
        protected List<AddressLine> addressLines;
        @XmlElement(name = "PostTownName", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = PostalCodeImpl.PostTownImpl.PostTownNameImpl.class)
        protected List<PostalCode.PostTown.PostTownName> postTownNames;
        @XmlElement(name = "PostTownSuffix", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = PostalCodeImpl.PostTownImpl.PostTownSuffixImpl.class)
        protected PostalCodeImpl.PostTownImpl.PostTownSuffixImpl postTownSuffix;
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

        public List<PostalCode.PostTown.PostTownName> getPostTownNames() {
            if (postTownNames == null) {
                postTownNames = new ArrayList<PostalCode.PostTown.PostTownName>();
            }
            return this.postTownNames;
        }

        public PostalCode.PostTown.PostTownSuffix getPostTownSuffix() {
            return postTownSuffix;
        }

        public void setPostTownSuffix(PostalCode.PostTown.PostTownSuffix value) {
            this.postTownSuffix = ((PostalCodeImpl.PostTownImpl.PostTownSuffixImpl) value);
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
        public static class PostTownNameImpl
            implements PostalCode.PostTown.PostTownName
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
        public static class PostTownSuffixImpl
            implements PostalCode.PostTown.PostTownSuffix
        {

            @XmlValue
            protected String content;
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

}