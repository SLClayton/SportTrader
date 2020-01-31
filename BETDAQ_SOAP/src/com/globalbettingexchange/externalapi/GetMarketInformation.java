
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
 *         &lt;element name="getMarketInformationRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetMarketInformationRequest" minOccurs="0"/&gt;
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
    "getMarketInformationRequest"
})
@XmlRootElement(name = "GetMarketInformation")
public class GetMarketInformation {

    protected GetMarketInformationRequest getMarketInformationRequest;

    /**
     * Gets the value of the getMarketInformationRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetMarketInformationRequest }
     *     
     */
    public GetMarketInformationRequest getGetMarketInformationRequest() {
        return getMarketInformationRequest;
    }

    /**
     * Sets the value of the getMarketInformationRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetMarketInformationRequest }
     *     
     */
    public void setGetMarketInformationRequest(GetMarketInformationRequest value) {
        this.getMarketInformationRequest = value;
    }

}
