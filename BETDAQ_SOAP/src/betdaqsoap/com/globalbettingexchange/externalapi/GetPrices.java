
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
 *         &lt;element name="getPricesRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetPricesRequest" minOccurs="0"/&gt;
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
    "getPricesRequest"
})
@XmlRootElement(name = "GetPrices")
public class GetPrices {

    protected GetPricesRequest getPricesRequest;

    /**
     * Gets the value of the getPricesRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetPricesRequest }
     *     
     */
    public GetPricesRequest getGetPricesRequest() {
        return getPricesRequest;
    }

    /**
     * Sets the value of the getPricesRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetPricesRequest }
     *     
     */
    public void setGetPricesRequest(GetPricesRequest value) {
        this.getPricesRequest = value;
    }

}
