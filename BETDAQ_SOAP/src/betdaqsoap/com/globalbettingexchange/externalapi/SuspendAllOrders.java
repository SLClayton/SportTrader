
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
 *         &lt;element name="suspendAllOrdersRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}SuspendAllOrdersRequest" minOccurs="0"/&gt;
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
    "suspendAllOrdersRequest"
})
@XmlRootElement(name = "SuspendAllOrders")
public class SuspendAllOrders {

    protected SuspendAllOrdersRequest suspendAllOrdersRequest;

    /**
     * Gets the value of the suspendAllOrdersRequest property.
     * 
     * @return
     *     possible object is
     *     {@link SuspendAllOrdersRequest }
     *     
     */
    public SuspendAllOrdersRequest getSuspendAllOrdersRequest() {
        return suspendAllOrdersRequest;
    }

    /**
     * Sets the value of the suspendAllOrdersRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link SuspendAllOrdersRequest }
     *     
     */
    public void setSuspendAllOrdersRequest(SuspendAllOrdersRequest value) {
        this.suspendAllOrdersRequest = value;
    }

}
