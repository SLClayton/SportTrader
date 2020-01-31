
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="cancelOrdersRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}CancelOrdersRequest" minOccurs="0"/&gt;
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
    "cancelOrdersRequest"
})
@XmlRootElement(name = "CancelOrders")
public class CancelOrders {

    protected CancelOrdersRequest cancelOrdersRequest;

    /**
     * Gets the value of the cancelOrdersRequest property.
     * 
     * @return
     *     possible object is
     *     {@link CancelOrdersRequest }
     *     
     */
    public CancelOrdersRequest getCancelOrdersRequest() {
        return cancelOrdersRequest;
    }

    /**
     * Sets the value of the cancelOrdersRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link CancelOrdersRequest }
     *     
     */
    public void setCancelOrdersRequest(CancelOrdersRequest value) {
        this.cancelOrdersRequest = value;
    }

}
