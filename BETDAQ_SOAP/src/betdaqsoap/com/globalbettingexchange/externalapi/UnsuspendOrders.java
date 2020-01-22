
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
 *         &lt;element name="unsuspendOrdersRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}UnsuspendOrdersRequest" minOccurs="0"/&gt;
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
    "unsuspendOrdersRequest"
})
@XmlRootElement(name = "UnsuspendOrders")
public class UnsuspendOrders {

    protected UnsuspendOrdersRequest unsuspendOrdersRequest;

    /**
     * Gets the value of the unsuspendOrdersRequest property.
     * 
     * @return
     *     possible object is
     *     {@link UnsuspendOrdersRequest }
     *     
     */
    public UnsuspendOrdersRequest getUnsuspendOrdersRequest() {
        return unsuspendOrdersRequest;
    }

    /**
     * Sets the value of the unsuspendOrdersRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnsuspendOrdersRequest }
     *     
     */
    public void setUnsuspendOrdersRequest(UnsuspendOrdersRequest value) {
        this.unsuspendOrdersRequest = value;
    }

}
