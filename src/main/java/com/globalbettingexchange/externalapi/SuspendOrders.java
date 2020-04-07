
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
 *         &lt;element name="suspendOrdersRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}SuspendOrdersRequest"/&gt;
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
    "suspendOrdersRequest"
})
@XmlRootElement(name = "SuspendOrders")
public class SuspendOrders {

    @XmlElement(required = true)
    protected SuspendOrdersRequest suspendOrdersRequest;

    /**
     * Gets the value of the suspendOrdersRequest property.
     * 
     * @return
     *     possible object is
     *     {@link SuspendOrdersRequest }
     *     
     */
    public SuspendOrdersRequest getSuspendOrdersRequest() {
        return suspendOrdersRequest;
    }

    /**
     * Sets the value of the suspendOrdersRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link SuspendOrdersRequest }
     *     
     */
    public void setSuspendOrdersRequest(SuspendOrdersRequest value) {
        this.suspendOrdersRequest = value;
    }

}
