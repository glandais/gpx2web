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
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import org.gpx2web.binding.kml.AddressLine;
import org.gpx2web.binding.kml.DependentLocalityType;
import org.gpx2web.binding.kml.FirmType;
import org.gpx2web.binding.kml.PostalCode;
import org.gpx2web.binding.kml.Premise;
import org.gpx2web.binding.kml.Thoroughfare;
import org.gpx2web.binding.kml.ThoroughfareLeadingTypeType;
import org.gpx2web.binding.kml.ThoroughfareNameType;
import org.gpx2web.binding.kml.ThoroughfareNumberPrefix;
import org.gpx2web.binding.kml.ThoroughfareNumberSuffix;
import org.gpx2web.binding.kml.ThoroughfarePostDirectionType;
import org.gpx2web.binding.kml.ThoroughfarePreDirectionType;
import org.gpx2web.binding.kml.ThoroughfareTrailingTypeType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "addressLines",
    "thoroughfareNumbersAndThoroughfareNumberRanges",
    "thoroughfareNumberPrefixes",
    "thoroughfareNumberSuffixes",
    "thoroughfarePreDirection",
    "thoroughfareLeadingType",
    "thoroughfareNames",
    "thoroughfareTrailingType",
    "thoroughfarePostDirection",
    "dependentThoroughfare",
    "postalCode",
    "firm",
    "premise",
    "dependentLocality",
    "anies"
})
@XmlRootElement(name = "Thoroughfare", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
public class ThoroughfareImpl
    implements Thoroughfare
{

    @XmlElement(name = "AddressLine", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = AddressLineImpl.class)
    protected List<AddressLine> addressLines;
    @XmlElements({
        @XmlElement(name = "ThoroughfareNumberRange", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareImpl.ThoroughfareNumberRangeImpl.class),
        @XmlElement(name = "ThoroughfareNumber", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareNumberImpl.class)
    })
    protected List<Object> thoroughfareNumbersAndThoroughfareNumberRanges;
    @XmlElement(name = "ThoroughfareNumberPrefix", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareNumberPrefixImpl.class)
    protected List<ThoroughfareNumberPrefix> thoroughfareNumberPrefixes;
    @XmlElement(name = "ThoroughfareNumberSuffix", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareNumberSuffixImpl.class)
    protected List<ThoroughfareNumberSuffix> thoroughfareNumberSuffixes;
    @XmlElement(name = "ThoroughfarePreDirection", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfarePreDirectionTypeImpl.class)
    protected ThoroughfarePreDirectionTypeImpl thoroughfarePreDirection;
    @XmlElement(name = "ThoroughfareLeadingType", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareLeadingTypeTypeImpl.class)
    protected ThoroughfareLeadingTypeTypeImpl thoroughfareLeadingType;
    @XmlElement(name = "ThoroughfareName", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareNameTypeImpl.class)
    protected List<ThoroughfareNameType> thoroughfareNames;
    @XmlElement(name = "ThoroughfareTrailingType", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareTrailingTypeTypeImpl.class)
    protected ThoroughfareTrailingTypeTypeImpl thoroughfareTrailingType;
    @XmlElement(name = "ThoroughfarePostDirection", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfarePostDirectionTypeImpl.class)
    protected ThoroughfarePostDirectionTypeImpl thoroughfarePostDirection;
    @XmlElement(name = "DependentThoroughfare", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareImpl.DependentThoroughfareImpl.class)
    protected ThoroughfareImpl.DependentThoroughfareImpl dependentThoroughfare;
    @XmlElement(name = "PostalCode", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = PostalCodeImpl.class)
    protected PostalCodeImpl postalCode;
    @XmlElement(name = "Firm", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = FirmTypeImpl.class)
    protected FirmTypeImpl firm;
    @XmlElement(name = "Premise", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = PremiseImpl.class)
    protected PremiseImpl premise;
    @XmlElement(name = "DependentLocality", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = DependentLocalityTypeImpl.class)
    protected DependentLocalityTypeImpl dependentLocality;
    @XmlAnyElement(lax = true)
    protected List<Object> anies;
    @XmlAttribute(name = "Type")
    @XmlSchemaType(name = "anySimpleType")
    protected String type;
    @XmlAttribute(name = "DependentThoroughfares")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String dependentThoroughfares;
    @XmlAttribute(name = "DependentThoroughfaresIndicator")
    @XmlSchemaType(name = "anySimpleType")
    protected String dependentThoroughfaresIndicator;
    @XmlAttribute(name = "DependentThoroughfaresConnector")
    @XmlSchemaType(name = "anySimpleType")
    protected String dependentThoroughfaresConnector;
    @XmlAttribute(name = "DependentThoroughfaresType")
    @XmlSchemaType(name = "anySimpleType")
    protected String dependentThoroughfaresType;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    public List<AddressLine> getAddressLines() {
        if (addressLines == null) {
            addressLines = new ArrayList<AddressLine>();
        }
        return this.addressLines;
    }

    public List<Object> getThoroughfareNumbersAndThoroughfareNumberRanges() {
        if (thoroughfareNumbersAndThoroughfareNumberRanges == null) {
            thoroughfareNumbersAndThoroughfareNumberRanges = new ArrayList<Object>();
        }
        return this.thoroughfareNumbersAndThoroughfareNumberRanges;
    }

    public List<ThoroughfareNumberPrefix> getThoroughfareNumberPrefixes() {
        if (thoroughfareNumberPrefixes == null) {
            thoroughfareNumberPrefixes = new ArrayList<ThoroughfareNumberPrefix>();
        }
        return this.thoroughfareNumberPrefixes;
    }

    public List<ThoroughfareNumberSuffix> getThoroughfareNumberSuffixes() {
        if (thoroughfareNumberSuffixes == null) {
            thoroughfareNumberSuffixes = new ArrayList<ThoroughfareNumberSuffix>();
        }
        return this.thoroughfareNumberSuffixes;
    }

    public ThoroughfarePreDirectionType getThoroughfarePreDirection() {
        return thoroughfarePreDirection;
    }

    public void setThoroughfarePreDirection(ThoroughfarePreDirectionType value) {
        this.thoroughfarePreDirection = ((ThoroughfarePreDirectionTypeImpl) value);
    }

    public ThoroughfareLeadingTypeType getThoroughfareLeadingType() {
        return thoroughfareLeadingType;
    }

    public void setThoroughfareLeadingType(ThoroughfareLeadingTypeType value) {
        this.thoroughfareLeadingType = ((ThoroughfareLeadingTypeTypeImpl) value);
    }

    public List<ThoroughfareNameType> getThoroughfareNames() {
        if (thoroughfareNames == null) {
            thoroughfareNames = new ArrayList<ThoroughfareNameType>();
        }
        return this.thoroughfareNames;
    }

    public ThoroughfareTrailingTypeType getThoroughfareTrailingType() {
        return thoroughfareTrailingType;
    }

    public void setThoroughfareTrailingType(ThoroughfareTrailingTypeType value) {
        this.thoroughfareTrailingType = ((ThoroughfareTrailingTypeTypeImpl) value);
    }

    public ThoroughfarePostDirectionType getThoroughfarePostDirection() {
        return thoroughfarePostDirection;
    }

    public void setThoroughfarePostDirection(ThoroughfarePostDirectionType value) {
        this.thoroughfarePostDirection = ((ThoroughfarePostDirectionTypeImpl) value);
    }

    public Thoroughfare.DependentThoroughfare getDependentThoroughfare() {
        return dependentThoroughfare;
    }

    public void setDependentThoroughfare(Thoroughfare.DependentThoroughfare value) {
        this.dependentThoroughfare = ((ThoroughfareImpl.DependentThoroughfareImpl) value);
    }

    public PostalCode getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(PostalCode value) {
        this.postalCode = ((PostalCodeImpl) value);
    }

    public FirmType getFirm() {
        return firm;
    }

    public void setFirm(FirmType value) {
        this.firm = ((FirmTypeImpl) value);
    }

    public Premise getPremise() {
        return premise;
    }

    public void setPremise(Premise value) {
        this.premise = ((PremiseImpl) value);
    }

    public DependentLocalityType getDependentLocality() {
        return dependentLocality;
    }

    public void setDependentLocality(DependentLocalityType value) {
        this.dependentLocality = ((DependentLocalityTypeImpl) value);
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

    public String getDependentThoroughfares() {
        return dependentThoroughfares;
    }

    public void setDependentThoroughfares(String value) {
        this.dependentThoroughfares = value;
    }

    public String getDependentThoroughfaresIndicator() {
        return dependentThoroughfaresIndicator;
    }

    public void setDependentThoroughfaresIndicator(String value) {
        this.dependentThoroughfaresIndicator = value;
    }

    public String getDependentThoroughfaresConnector() {
        return dependentThoroughfaresConnector;
    }

    public void setDependentThoroughfaresConnector(String value) {
        this.dependentThoroughfaresConnector = value;
    }

    public String getDependentThoroughfaresType() {
        return dependentThoroughfaresType;
    }

    public void setDependentThoroughfaresType(String value) {
        this.dependentThoroughfaresType = value;
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
        "addressLines",
        "thoroughfarePreDirection",
        "thoroughfareLeadingType",
        "thoroughfareNames",
        "thoroughfareTrailingType",
        "thoroughfarePostDirection",
        "anies"
    })
    public static class DependentThoroughfareImpl
        implements Thoroughfare.DependentThoroughfare
    {

        @XmlElement(name = "AddressLine", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = AddressLineImpl.class)
        protected List<AddressLine> addressLines;
        @XmlElement(name = "ThoroughfarePreDirection", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfarePreDirectionTypeImpl.class)
        protected ThoroughfarePreDirectionTypeImpl thoroughfarePreDirection;
        @XmlElement(name = "ThoroughfareLeadingType", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareLeadingTypeTypeImpl.class)
        protected ThoroughfareLeadingTypeTypeImpl thoroughfareLeadingType;
        @XmlElement(name = "ThoroughfareName", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareNameTypeImpl.class)
        protected List<ThoroughfareNameType> thoroughfareNames;
        @XmlElement(name = "ThoroughfareTrailingType", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareTrailingTypeTypeImpl.class)
        protected ThoroughfareTrailingTypeTypeImpl thoroughfareTrailingType;
        @XmlElement(name = "ThoroughfarePostDirection", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfarePostDirectionTypeImpl.class)
        protected ThoroughfarePostDirectionTypeImpl thoroughfarePostDirection;
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

        public ThoroughfarePreDirectionType getThoroughfarePreDirection() {
            return thoroughfarePreDirection;
        }

        public void setThoroughfarePreDirection(ThoroughfarePreDirectionType value) {
            this.thoroughfarePreDirection = ((ThoroughfarePreDirectionTypeImpl) value);
        }

        public ThoroughfareLeadingTypeType getThoroughfareLeadingType() {
            return thoroughfareLeadingType;
        }

        public void setThoroughfareLeadingType(ThoroughfareLeadingTypeType value) {
            this.thoroughfareLeadingType = ((ThoroughfareLeadingTypeTypeImpl) value);
        }

        public List<ThoroughfareNameType> getThoroughfareNames() {
            if (thoroughfareNames == null) {
                thoroughfareNames = new ArrayList<ThoroughfareNameType>();
            }
            return this.thoroughfareNames;
        }

        public ThoroughfareTrailingTypeType getThoroughfareTrailingType() {
            return thoroughfareTrailingType;
        }

        public void setThoroughfareTrailingType(ThoroughfareTrailingTypeType value) {
            this.thoroughfareTrailingType = ((ThoroughfareTrailingTypeTypeImpl) value);
        }

        public ThoroughfarePostDirectionType getThoroughfarePostDirection() {
            return thoroughfarePostDirection;
        }

        public void setThoroughfarePostDirection(ThoroughfarePostDirectionType value) {
            this.thoroughfarePostDirection = ((ThoroughfarePostDirectionTypeImpl) value);
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

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "addressLines",
        "thoroughfareNumberFrom",
        "thoroughfareNumberTo"
    })
    public static class ThoroughfareNumberRangeImpl implements Thoroughfare.ThoroughfareNumberRange
    {

        @XmlElement(name = "AddressLine", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = AddressLineImpl.class)
        protected List<AddressLine> addressLines;
        @XmlElement(name = "ThoroughfareNumberFrom", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", required = true, type = ThoroughfareImpl.ThoroughfareNumberRangeImpl.ThoroughfareNumberFromImpl.class)
        protected ThoroughfareImpl.ThoroughfareNumberRangeImpl.ThoroughfareNumberFromImpl thoroughfareNumberFrom;
        @XmlElement(name = "ThoroughfareNumberTo", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", required = true, type = ThoroughfareImpl.ThoroughfareNumberRangeImpl.ThoroughfareNumberToImpl.class)
        protected ThoroughfareImpl.ThoroughfareNumberRangeImpl.ThoroughfareNumberToImpl thoroughfareNumberTo;
        @XmlAttribute(name = "RangeType")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String rangeType;
        @XmlAttribute(name = "Indicator")
        @XmlSchemaType(name = "anySimpleType")
        protected String indicator;
        @XmlAttribute(name = "Separator")
        @XmlSchemaType(name = "anySimpleType")
        protected String separator;
        @XmlAttribute(name = "IndicatorOccurrence")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String indicatorOccurrence;
        @XmlAttribute(name = "NumberRangeOccurrence")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String numberRangeOccurrence;
        @XmlAttribute(name = "Type")
        @XmlSchemaType(name = "anySimpleType")
        protected String type;
        @XmlAttribute(name = "Code")
        @XmlSchemaType(name = "anySimpleType")
        protected String code;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();

        public List<AddressLine> getAddressLines() {
            if (addressLines == null) {
                addressLines = new ArrayList<AddressLine>();
            }
            return this.addressLines;
        }

        public Thoroughfare.ThoroughfareNumberRange.ThoroughfareNumberFrom getThoroughfareNumberFrom() {
            return thoroughfareNumberFrom;
        }

        public void setThoroughfareNumberFrom(Thoroughfare.ThoroughfareNumberRange.ThoroughfareNumberFrom value) {
            this.thoroughfareNumberFrom = ((ThoroughfareImpl.ThoroughfareNumberRangeImpl.ThoroughfareNumberFromImpl) value);
        }

        public Thoroughfare.ThoroughfareNumberRange.ThoroughfareNumberTo getThoroughfareNumberTo() {
            return thoroughfareNumberTo;
        }

        public void setThoroughfareNumberTo(Thoroughfare.ThoroughfareNumberRange.ThoroughfareNumberTo value) {
            this.thoroughfareNumberTo = ((ThoroughfareImpl.ThoroughfareNumberRangeImpl.ThoroughfareNumberToImpl) value);
        }

        public String getRangeType() {
            return rangeType;
        }

        public void setRangeType(String value) {
            this.rangeType = value;
        }

        public String getIndicator() {
            return indicator;
        }

        public void setIndicator(String value) {
            this.indicator = value;
        }

        public String getSeparator() {
            return separator;
        }

        public void setSeparator(String value) {
            this.separator = value;
        }

        public String getIndicatorOccurrence() {
            return indicatorOccurrence;
        }

        public void setIndicatorOccurrence(String value) {
            this.indicatorOccurrence = value;
        }

        public String getNumberRangeOccurrence() {
            return numberRangeOccurrence;
        }

        public void setNumberRangeOccurrence(String value) {
            this.numberRangeOccurrence = value;
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

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "content"
        })
        public static class ThoroughfareNumberFromImpl
            implements Thoroughfare.ThoroughfareNumberRange.ThoroughfareNumberFrom
        {

            @XmlElementRefs({
                @XmlElementRef(name = "ThoroughfareNumberPrefix", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareNumberPrefixImpl.class),
                @XmlElementRef(name = "ThoroughfareNumber", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareNumberImpl.class),
                @XmlElementRef(name = "ThoroughfareNumberSuffix", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareNumberSuffixImpl.class),
                @XmlElementRef(name = "AddressLine", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = AddressLineImpl.class)
            })
            @XmlMixed
            protected List<Object> content;
            @XmlAttribute(name = "Code")
            @XmlSchemaType(name = "anySimpleType")
            protected String code;
            @XmlAnyAttribute
            private Map<QName, String> otherAttributes = new HashMap<QName, String>();

            public List<Object> getContent() {
                if (content == null) {
                    content = new ArrayList<Object>();
                }
                return this.content;
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
        public static class ThoroughfareNumberToImpl
            implements Thoroughfare.ThoroughfareNumberRange.ThoroughfareNumberTo
        {

            @XmlElementRefs({
                @XmlElementRef(name = "ThoroughfareNumberPrefix", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareNumberPrefixImpl.class),
                @XmlElementRef(name = "ThoroughfareNumber", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareNumberImpl.class),
                @XmlElementRef(name = "ThoroughfareNumberSuffix", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = ThoroughfareNumberSuffixImpl.class),
                @XmlElementRef(name = "AddressLine", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = AddressLineImpl.class)
            })
            @XmlMixed
            protected List<Object> content;
            @XmlAttribute(name = "Code")
            @XmlSchemaType(name = "anySimpleType")
            protected String code;
            @XmlAnyAttribute
            private Map<QName, String> otherAttributes = new HashMap<QName, String>();

            public List<Object> getContent() {
                if (content == null) {
                    content = new ArrayList<Object>();
                }
                return this.content;
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