
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
 *         &lt;element name="getOrderDetailsRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetOrderDetailsRequest" minOccurs="0"/&gt;
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
    "getOrderDetailsRequest"
})
@XmlRootElement(name = "GetOrderDetails")
public class GetOrderDetails {

    protected GetOrderDetailsRequest getOrderDetailsRequest;

    /**
     * Gets the value of the getOrderDetailsRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetOrderDetailsRequest }
     *     
     */
    public GetOrderDetailsRequest getGetOrderDetailsRequest() {
        return getOrderDetailsRequest;
    }

    /**
     * Sets the value of the getOrderDetailsRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetOrderDetailsRequest }
     *     
     */
    public void setGetOrderDetailsRequest(GetOrderDetailsRequest value) {
        this.getOrderDetailsRequest = value;
    }

}
