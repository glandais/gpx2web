//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.09.22 at 04:04:36 PM CEST 
//


package org.gpx2web.binding.kml;

import java.util.List;


/**
 * <p>Java class for RegionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LatLonAltBox" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Lod" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}RegionSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}RegionObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface RegionType extends AbstractObjectType
{


    /**
     * Gets the value of the latLonAltBox property.
     * 
     * @return
     *     possible object is
     *     {@link LatLonAltBoxType }
     *     
     */
    LatLonAltBoxType getLatLonAltBox();

    /**
     * Sets the value of the latLonAltBox property.
     * 
     * @param value
     *     allowed object is
     *     {@link LatLonAltBoxType }
     *     
     */
    void setLatLonAltBox(LatLonAltBoxType value);

    /**
     * Gets the value of the lod property.
     * 
     * @return
     *     possible object is
     *     {@link LodType }
     *     
     */
    LodType getLod();

    /**
     * Sets the value of the lod property.
     * 
     * @param value
     *     allowed object is
     *     {@link LodType }
     *     
     */
    void setLod(LodType value);

    /**
     * Gets the value of the regionSimpleExtensionGroups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the regionSimpleExtensionGroups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRegionSimpleExtensionGroups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    List<Object> getRegionSimpleExtensionGroups();

    /**
     * Gets the value of the regionObjectExtensionGroups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the regionObjectExtensionGroups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRegionObjectExtensionGroups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    List<AbstractObjectType> getRegionObjectExtensionGroups();

}