
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
 *         &lt;element name="GetOrderDetailsResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetOrderDetailsResponse" minOccurs="0"/&gt;
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
    "getOrderDetailsResult"
})
@XmlRootElement(name = "GetOrderDetailsResponse")
public class GetOrderDetailsResponse {

    @XmlElement(name = "GetOrderDetailsResult")
    protected GetOrderDetailsResponse2 getOrderDetailsResult;

    /**
     * Gets the value of the getOrderDetailsResult property.
     * 
     * @return
     *     possible object is
     *     {@link GetOrderDetailsResponse2 }
     *     
     */
    public GetOrderDetailsResponse2 getGetOrderDetailsResult() {
        return getOrderDetailsResult;
    }

    /**
     * Sets the value of the getOrderDetailsResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetOrderDetailsResponse2 }
     *     
     */
    public void setGetOrderDetailsResult(GetOrderDetailsResponse2 value) {
        this.getOrderDetailsResult = value;
    }

}
