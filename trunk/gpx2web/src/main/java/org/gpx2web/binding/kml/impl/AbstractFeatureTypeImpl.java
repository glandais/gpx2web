//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.09.22 at 04:04:36 PM CEST 
//


package org.gpx2web.binding.kml.impl;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.gpx2web.binding.kml.AbstractFeatureType;
import org.gpx2web.binding.kml.AbstractObjectType;
import org.gpx2web.binding.kml.AbstractStyleSelectorType;
import org.gpx2web.binding.kml.AbstractTimePrimitiveType;
import org.gpx2web.binding.kml.AbstractViewType;
import org.gpx2web.binding.kml.AddressDetails;
import org.gpx2web.binding.kml.Author;
import org.gpx2web.binding.kml.ExtendedData;
import org.gpx2web.binding.kml.Link;
import org.gpx2web.binding.kml.Metadata;
import org.gpx2web.binding.kml.RegionType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractFeatureType", propOrder = {
    "name",
    "visibility",
    "open",
    "author",
    "link",
    "address",
    "addressDetails",
    "phoneNumber",
    "snippet",
    "description",
    "abstractViewGroup",
    "abstractTimePrimitiveGroup",
    "styleUrl",
    "abstractStyleSelectorGroups",
    "region",
    "extendedData",
    "metadata",
    "abstractFeatureSimpleExtensionGroups",
    "abstractFeatureObjectExtensionGroups"
})
@XmlSeeAlso({
    NetworkLinkTypeImpl.class,
    AbstractOverlayTypeImpl.class,
    AbstractContainerTypeImpl.class,
    PlacemarkTypeImpl.class
})
public abstract class AbstractFeatureTypeImpl
    extends AbstractObjectTypeImpl
    implements AbstractFeatureType
{

    protected String name;
    @XmlElement(defaultValue = "1")
    protected Boolean visibility;
    @XmlElement(defaultValue = "0")
    protected Boolean open;
    @XmlElement(namespace = "http://www.w3.org/2005/Atom", type = AuthorImpl.class)
    protected AuthorImpl author;
    @XmlElement(namespace = "http://www.w3.org/2005/Atom", type = LinkImpl.class)
    protected LinkImpl link;
    protected String address;
    @XmlElement(name = "AddressDetails", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", type = AddressDetailsImpl.class)
    protected AddressDetailsImpl addressDetails;
    protected String phoneNumber;
    protected String snippet;
    protected String description;
    @XmlElementRef(name = "AbstractViewGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class)
    protected JAXBElement<? extends AbstractViewTypeImpl> abstractViewGroup;
    @XmlElementRef(name = "AbstractTimePrimitiveGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class)
    protected JAXBElement<? extends AbstractTimePrimitiveTypeImpl> abstractTimePrimitiveGroup;
    @XmlSchemaType(name = "anyURI")
    protected String styleUrl;
    @XmlElementRef(name = "AbstractStyleSelectorGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class)
    protected List<JAXBElement<? extends AbstractStyleSelectorType>> abstractStyleSelectorGroups;
    @XmlElement(name = "Region", type = RegionTypeImpl.class)
    protected RegionTypeImpl region;
    @XmlElement(name = "ExtendedData", type = ExtendedDataImpl.class)
    protected ExtendedDataImpl extendedData;
    @XmlElement(name = "Metadata", type = MetadataImpl.class)
    protected MetadataImpl metadata;
    @XmlElement(name = "AbstractFeatureSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> abstractFeatureSimpleExtensionGroups;
    @XmlElement(name = "AbstractFeatureObjectExtensionGroup", type = AbstractObjectTypeImpl.class)
    protected List<AbstractObjectType> abstractFeatureObjectExtensionGroups;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public Boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean value) {
        this.visibility = value;
    }

    public Boolean isOpen() {
        return open;
    }

    public void setOpen(Boolean value) {
        this.open = value;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author value) {
        this.author = ((AuthorImpl) value);
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link value) {
        this.link = ((LinkImpl) value);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String value) {
        this.address = value;
    }

    public AddressDetails getAddressDetails() {
        return addressDetails;
    }

    public void setAddressDetails(AddressDetails value) {
        this.addressDetails = ((AddressDetailsImpl) value);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String value) {
        this.phoneNumber = value;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String value) {
        this.snippet = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public JAXBElement<? extends AbstractViewType> getAbstractViewGroup() {
        return abstractViewGroup;
    }

    public void setAbstractViewGroup(JAXBElement<? extends AbstractViewType> value) {
        this.abstractViewGroup = ((JAXBElement<? extends AbstractViewTypeImpl> ) value);
    }

    public JAXBElement<? extends AbstractTimePrimitiveType> getAbstractTimePrimitiveGroup() {
        return abstractTimePrimitiveGroup;
    }

    public void setAbstractTimePrimitiveGroup(JAXBElement<? extends AbstractTimePrimitiveType> value) {
        this.abstractTimePrimitiveGroup = ((JAXBElement<? extends AbstractTimePrimitiveTypeImpl> ) value);
    }

    public String getStyleUrl() {
        return styleUrl;
    }

    public void setStyleUrl(String value) {
        this.styleUrl = value;
    }

    public List<JAXBElement<? extends AbstractStyleSelectorType>> getAbstractStyleSelectorGroups() {
        if (abstractStyleSelectorGroups == null) {
            abstractStyleSelectorGroups = new ArrayList<JAXBElement<? extends AbstractStyleSelectorType>>();
        }
        return this.abstractStyleSelectorGroups;
    }

    public RegionType getRegion() {
        return region;
    }

    public void setRegion(RegionType value) {
        this.region = ((RegionTypeImpl) value);
    }

    public ExtendedData getExtendedData() {
        return extendedData;
    }

    public void setExtendedData(ExtendedData value) {
        this.extendedData = ((ExtendedDataImpl) value);
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata value) {
        this.metadata = ((MetadataImpl) value);
    }

    public List<Object> getAbstractFeatureSimpleExtensionGroups() {
        if (abstractFeatureSimpleExtensionGroups == null) {
            abstractFeatureSimpleExtensionGroups = new ArrayList<Object>();
        }
        return this.abstractFeatureSimpleExtensionGroups;
    }

    public List<AbstractObjectType> getAbstractFeatureObjectExtensionGroups() {
        if (abstractFeatureObjectExtensionGroups == null) {
            abstractFeatureObjectExtensionGroups = new ArrayList<AbstractObjectType>();
        }
        return this.abstractFeatureObjectExtensionGroups;
    }

}