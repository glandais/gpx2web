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
 * <p>Java class for CameraType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CameraType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractViewType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}longitude" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}latitude" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}altitude" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}heading" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}tilt" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}roll" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}altitudeModeGroup" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}CameraSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}CameraObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface CameraType extends AbstractViewType
{


    /**
     * Gets the value of the longitude property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    Double getLongitude();

    /**
     * Sets the value of the longitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    void setLongitude(Double value);

    /**
     * Gets the value of the latitude property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    Double getLatitude();

    /**
     * Sets the value of the latitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    void setLatitude(Double value);

    /**
     * Gets the value of the altitude property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    Double getAltitude();

    /**
     * Sets the value of the altitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    void setAltitude(Double value);

    /**
     * Gets the value of the heading property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    Double getHeading();

    /**
     * Sets the value of the heading property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    void setHeading(Double value);

    /**
     * Gets the value of the tilt property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    Double getTilt();

    /**
     * Sets the value of the tilt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    void setTilt(Double value);

    /**
     * Gets the value of the roll property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    Double getRoll();

    /**
     * Sets the value of the roll property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    void setRoll(Double value);

    /**
     * Gets the value of the altitudeModeGroup property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link AltitudeModeEnumType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     
     */
    JAXBElement<?> getAltitudeModeGroup();

    /**
     * Sets the value of the altitudeModeGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link AltitudeModeEnumType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     
     */
    void setAltitudeModeGroup(JAXBElement<?> value);

    /**
     * Gets the value of the cameraSimpleExtensionGroups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cameraSimpleExtensionGroups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCameraSimpleExtensionGroups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    List<Object> getCameraSimpleExtensionGroups();

    /**
     * Gets the value of the cameraObjectExtensionGroups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cameraObjectExtensionGroups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCameraObjectExtensionGroups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    List<AbstractObjectType> getCameraObjectExtensionGroups();

}