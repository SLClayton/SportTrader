
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
 *         &lt;element name="GetMarketInformationResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetMarketInformationResponse" minOccurs="0"/&gt;
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
    "getMarketInformationResult"
})
@XmlRootElement(name = "GetMarketInformationResponse")
public class GetMarketInformationResponse {

    @XmlElement(name = "GetMarketInformationResult")
    protected GetMarketInformationResponse2 getMarketInformationResult;

    /**
     * Gets the value of the getMarketInformationResult property.
     * 
     * @return
     *     possible object is
     *     {@link GetMarketInformationResponse2 }
     *     
     */
    public GetMarketInformationResponse2 getGetMarketInformationResult() {
        return getMarketInformationResult;
    }

    /**
     * Sets the value of the getMarketInformationResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetMarketInformationResponse2 }
     *     
     */
    public void setGetMarketInformationResult(GetMarketInformationResponse2 value) {
        this.getMarketInformationResult = value;
    }

}
