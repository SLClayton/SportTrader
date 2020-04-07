
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;


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
 *         &lt;element name="PlaceOrdersWithReceiptResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}PlaceOrdersWithReceiptResponse" minOccurs="0"/&gt;
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
    "placeOrdersWithReceiptResult"
})
@XmlRootElement(name = "PlaceOrdersWithReceiptResponse")
public class PlaceOrdersWithReceiptResponse {

    @XmlElement(name = "PlaceOrdersWithReceiptResult")
    protected PlaceOrdersWithReceiptResponse2 placeOrdersWithReceiptResult;

    /**
     * Gets the value of the placeOrdersWithReceiptResult property.
     * 
     * @return
     *     possible object is
     *     {@link PlaceOrdersWithReceiptResponse2 }
     *     
     */
    public PlaceOrdersWithReceiptResponse2 getPlaceOrdersWithReceiptResult() {
        return placeOrdersWithReceiptResult;
    }

    /**
     * Sets the value of the placeOrdersWithReceiptResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link PlaceOrdersWithReceiptResponse2 }
     *     
     */
    public void setPlaceOrdersWithReceiptResult(PlaceOrdersWithReceiptResponse2 value) {
        this.placeOrdersWithReceiptResult = value;
    }

}
