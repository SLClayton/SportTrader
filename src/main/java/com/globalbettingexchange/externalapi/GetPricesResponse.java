
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
 *         &lt;element name="GetPricesResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetPricesResponse" minOccurs="0"/&gt;
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
    "getPricesResult"
})
@XmlRootElement(name = "GetPricesResponse")
public class GetPricesResponse {

    @XmlElement(name = "GetPricesResult")
    protected GetPricesResponse2 getPricesResult;

    /**
     * Gets the value of the getPricesResult property.
     * 
     * @return
     *     possible object is
     *     {@link GetPricesResponse2 }
     *     
     */
    public GetPricesResponse2 getGetPricesResult() {
        return getPricesResult;
    }

    /**
     * Sets the value of the getPricesResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetPricesResponse2 }
     *     
     */
    public void setGetPricesResult(GetPricesResponse2 value) {
        this.getPricesResult = value;
    }

}
