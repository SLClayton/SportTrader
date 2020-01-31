
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
 *         &lt;element name="CancelAllOrdersResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}CancelAllOrdersResponse" minOccurs="0"/&gt;
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
    "cancelAllOrdersResult"
})
@XmlRootElement(name = "CancelAllOrdersResponse")
public class CancelAllOrdersResponse {

    @XmlElement(name = "CancelAllOrdersResult")
    protected CancelAllOrdersResponse2 cancelAllOrdersResult;

    /**
     * Gets the value of the cancelAllOrdersResult property.
     * 
     * @return
     *     possible object is
     *     {@link CancelAllOrdersResponse2 }
     *     
     */
    public CancelAllOrdersResponse2 getCancelAllOrdersResult() {
        return cancelAllOrdersResult;
    }

    /**
     * Sets the value of the cancelAllOrdersResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link CancelAllOrdersResponse2 }
     *     
     */
    public void setCancelAllOrdersResult(CancelAllOrdersResponse2 value) {
        this.cancelAllOrdersResult = value;
    }

}
