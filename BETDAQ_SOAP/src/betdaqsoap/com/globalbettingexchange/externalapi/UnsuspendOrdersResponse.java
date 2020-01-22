
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="UnsuspendOrdersResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}UnsuspendOrdersResponse" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "unsuspendOrdersResult"
})
@XmlRootElement(name = "UnsuspendOrdersResponse")
public class UnsuspendOrdersResponse {

    @XmlElement(name = "UnsuspendOrdersResult")
    protected UnsuspendOrdersResponse2 unsuspendOrdersResult;

    /**
     * Gets the value of the unsuspendOrdersResult property.
     * 
     * @return
     *     possible object is
     *     {@link UnsuspendOrdersResponse2 }
     *     
     */
    public UnsuspendOrdersResponse2 getUnsuspendOrdersResult() {
        return unsuspendOrdersResult;
    }

    /**
     * Sets the value of the unsuspendOrdersResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnsuspendOrdersResponse2 }
     *     
     */
    public void setUnsuspendOrdersResult(UnsuspendOrdersResponse2 value) {
        this.unsuspendOrdersResult = value;
    }

}
