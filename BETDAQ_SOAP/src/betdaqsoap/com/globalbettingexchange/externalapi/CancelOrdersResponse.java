
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
 *         &lt;element name="CancelOrdersResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}CancelOrdersResponse" minOccurs="0"/&gt;
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
    "cancelOrdersResult"
})
@XmlRootElement(name = "CancelOrdersResponse")
public class CancelOrdersResponse {

    @XmlElement(name = "CancelOrdersResult")
    protected CancelOrdersResponse2 cancelOrdersResult;

    /**
     * Gets the value of the cancelOrdersResult property.
     * 
     * @return
     *     possible object is
     *     {@link CancelOrdersResponse2 }
     *     
     */
    public CancelOrdersResponse2 getCancelOrdersResult() {
        return cancelOrdersResult;
    }

    /**
     * Sets the value of the cancelOrdersResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link CancelOrdersResponse2 }
     *     
     */
    public void setCancelOrdersResult(CancelOrdersResponse2 value) {
        this.cancelOrdersResult = value;
    }

}
