
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
 *         &lt;element name="GetSPEnabledMarketsInformationResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetSPEnabledMarketsInformationResponse" minOccurs="0"/&gt;
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
    "getSPEnabledMarketsInformationResult"
})
@XmlRootElement(name = "GetSPEnabledMarketsInformationResponse")
public class GetSPEnabledMarketsInformationResponse {

    @XmlElement(name = "GetSPEnabledMarketsInformationResult")
    protected GetSPEnabledMarketsInformationResponse2 getSPEnabledMarketsInformationResult;

    /**
     * Gets the value of the getSPEnabledMarketsInformationResult property.
     * 
     * @return
     *     possible object is
     *     {@link GetSPEnabledMarketsInformationResponse2 }
     *     
     */
    public GetSPEnabledMarketsInformationResponse2 getGetSPEnabledMarketsInformationResult() {
        return getSPEnabledMarketsInformationResult;
    }

    /**
     * Sets the value of the getSPEnabledMarketsInformationResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetSPEnabledMarketsInformationResponse2 }
     *     
     */
    public void setGetSPEnabledMarketsInformationResult(GetSPEnabledMarketsInformationResponse2 value) {
        this.getSPEnabledMarketsInformationResult = value;
    }

}
