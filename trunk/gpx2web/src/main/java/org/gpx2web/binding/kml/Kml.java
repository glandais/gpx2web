//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.09.22 at 04:04:36 PM CEST 
//


package org.gpx2web.binding.kml;

import java.util.List;
import javax.xml.bind.JAXBElement;


/**
 * <p>Java class for KmlType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="KmlType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}NetworkLinkControl" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractFeatureGroup" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}KmlSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}KmlObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="hint" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface Kml {


    /**
     * Gets the value of the networkLinkControl property.
     * 
     * @return
     *     possible object is
     *     {@link NetworkLinkControl }
     *     
     */
    NetworkLinkControl getNetworkLinkControl();

    /**
     * Sets the value of the networkLinkControl property.
     * 
     * @param value
     *     allowed object is
     *     {@link NetworkLinkControl }
     *     
     */
    void setNetworkLinkControl(NetworkLinkControl value);

    /**
     * Gets the value of the abstractFeatureGroup property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link FolderType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ScreenOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link NetworkLinkType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DocumentType }{@code >}
     *     {@link JAXBElement }{@code <}{@link GroundOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractContainerType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PhotoOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractFeatureType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PlacemarkType }{@code >}
     *     
     */
    JAXBElement<? extends AbstractFeatureType> getAbstractFeatureGroup();

    /**
     * Sets the value of the abstractFeatureGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link FolderType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ScreenOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link NetworkLinkType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DocumentType }{@code >}
     *     {@link JAXBElement }{@code <}{@link GroundOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractContainerType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PhotoOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractFeatureType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractOverlayType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PlacemarkType }{@code >}
     *     
     */
    void setAbstractFeatureGroup(JAXBElement<? extends AbstractFeatureType> value);

    /**
     * Gets the value of the kmlSimpleExtensionGroups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the kmlSimpleExtensionGroups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKmlSimpleExtensionGroups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    List<Object> getKmlSimpleExtensionGroups();

    /**
     * Gets the value of the kmlObjectExtensionGroups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the kmlObjectExtensionGroups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKmlObjectExtensionGroups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    List<AbstractObjectType> getKmlObjectExtensionGroups();

    /**
     * Gets the value of the hint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    String getHint();

    /**
     * Sets the value of the hint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    void setHint(String value);

}