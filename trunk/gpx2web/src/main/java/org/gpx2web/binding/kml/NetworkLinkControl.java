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
 * <p>Java class for NetworkLinkControlType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NetworkLinkControlType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}minRefreshPeriod" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}maxSessionLength" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}cookie" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}message" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}linkName" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}linkDescription" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}linkSnippet" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}expires" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Update" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractViewGroup" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}NetworkLinkControlSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}NetworkLinkControlObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface NetworkLinkControl {


    /**
     * Gets the value of the minRefreshPeriod property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    Double getMinRefreshPeriod();

    /**
     * Sets the value of the minRefreshPeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    void setMinRefreshPeriod(Double value);

    /**
     * Gets the value of the maxSessionLength property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    Double getMaxSessionLength();

    /**
     * Sets the value of the maxSessionLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    void setMaxSessionLength(Double value);

    /**
     * Gets the value of the cookie property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    String getCookie();

    /**
     * Sets the value of the cookie property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    void setCookie(String value);

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    String getMessage();

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    void setMessage(String value);

    /**
     * Gets the value of the linkName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    String getLinkName();

    /**
     * Sets the value of the linkName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    void setLinkName(String value);

    /**
     * Gets the value of the linkDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    String getLinkDescription();

    /**
     * Sets the value of the linkDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    void setLinkDescription(String value);

    /**
     * Gets the value of the linkSnippet property.
     * 
     * @return
     *     possible object is
     *     {@link SnippetType }
     *     
     */
    SnippetType getLinkSnippet();

    /**
     * Sets the value of the linkSnippet property.
     * 
     * @param value
     *     allowed object is
     *     {@link SnippetType }
     *     
     */
    void setLinkSnippet(SnippetType value);

    /**
     * Gets the value of the expires property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    String getExpires();

    /**
     * Sets the value of the expires property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    void setExpires(String value);

    /**
     * Gets the value of the update property.
     * 
     * @return
     *     possible object is
     *     {@link Update }
     *     
     */
    Update getUpdate();

    /**
     * Sets the value of the update property.
     * 
     * @param value
     *     allowed object is
     *     {@link Update }
     *     
     */
    void setUpdate(Update value);

    /**
     * Gets the value of the abstractViewGroup property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link AbstractViewType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LookAtType }{@code >}
     *     {@link JAXBElement }{@code <}{@link CameraType }{@code >}
     *     
     */
    JAXBElement<? extends AbstractViewType> getAbstractViewGroup();

    /**
     * Sets the value of the abstractViewGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link AbstractViewType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LookAtType }{@code >}
     *     {@link JAXBElement }{@code <}{@link CameraType }{@code >}
     *     
     */
    void setAbstractViewGroup(JAXBElement<? extends AbstractViewType> value);

    /**
     * Gets the value of the networkLinkControlSimpleExtensionGroups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the networkLinkControlSimpleExtensionGroups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNetworkLinkControlSimpleExtensionGroups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    List<Object> getNetworkLinkControlSimpleExtensionGroups();

    /**
     * Gets the value of the networkLinkControlObjectExtensionGroups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the networkLinkControlObjectExtensionGroups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNetworkLinkControlObjectExtensionGroups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    List<AbstractObjectType> getNetworkLinkControlObjectExtensionGroups();

}