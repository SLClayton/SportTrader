
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
 *         &lt;element name="PlaceOrdersNoReceiptResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}PlaceOrdersNoReceiptResponse" minOccurs="0"/&gt;
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
    "placeOrdersNoReceiptResult"
})
@XmlRootElement(name = "PlaceOrdersNoReceiptResponse")
public class PlaceOrdersNoReceiptResponse {

    @XmlElement(name = "PlaceOrdersNoReceiptResult")
    protected PlaceOrdersNoReceiptResponse2 placeOrdersNoReceiptResult;

    /**
     * Gets the value of the placeOrdersNoReceiptResult property.
     * 
     * @return
     *     possible object is
     *     {@link PlaceOrdersNoReceiptResponse2 }
     *     
     */
    public PlaceOrdersNoReceiptResponse2 getPlaceOrdersNoReceiptResult() {
        return placeOrdersNoReceiptResult;
    }

    /**
     * Sets the value of the placeOrdersNoReceiptResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link PlaceOrdersNoReceiptResponse2 }
     *     
     */
    public void setPlaceOrdersNoReceiptResult(PlaceOrdersNoReceiptResponse2 value) {
        this.placeOrdersNoReceiptResult = value;
    }

}
