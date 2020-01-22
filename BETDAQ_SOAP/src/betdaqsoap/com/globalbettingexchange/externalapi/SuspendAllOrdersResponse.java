
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
 *         &lt;element name="SuspendAllOrdersResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}SuspendAllOrdersResponse" minOccurs="0"/&gt;
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
    "suspendAllOrdersResult"
})
@XmlRootElement(name = "SuspendAllOrdersResponse")
public class SuspendAllOrdersResponse {

    @XmlElement(name = "SuspendAllOrdersResult")
    protected SuspendAllOrdersResponse2 suspendAllOrdersResult;

    /**
     * Gets the value of the suspendAllOrdersResult property.
     * 
     * @return
     *     possible object is
     *     {@link SuspendAllOrdersResponse2 }
     *     
     */
    public SuspendAllOrdersResponse2 getSuspendAllOrdersResult() {
        return suspendAllOrdersResult;
    }

    /**
     * Sets the value of the suspendAllOrdersResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link SuspendAllOrdersResponse2 }
     *     
     */
    public void setSuspendAllOrdersResult(SuspendAllOrdersResponse2 value) {
        this.suspendAllOrdersResult = value;
    }

}
