
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
 *         &lt;element name="GetSPEnabledMarketsInformationRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetSPEnabledMarketsInformationRequest" minOccurs="0"/&gt;
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
    "getSPEnabledMarketsInformationRequest"
})
@XmlRootElement(name = "GetSPEnabledMarketsInformation")
public class GetSPEnabledMarketsInformation {

    @XmlElement(name = "GetSPEnabledMarketsInformationRequest")
    protected GetSPEnabledMarketsInformationRequest getSPEnabledMarketsInformationRequest;

    /**
     * Gets the value of the getSPEnabledMarketsInformationRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetSPEnabledMarketsInformationRequest }
     *     
     */
    public GetSPEnabledMarketsInformationRequest getGetSPEnabledMarketsInformationRequest() {
        return getSPEnabledMarketsInformationRequest;
    }

    /**
     * Sets the value of the getSPEnabledMarketsInformationRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetSPEnabledMarketsInformationRequest }
     *     
     */
    public void setGetSPEnabledMarketsInformationRequest(GetSPEnabledMarketsInformationRequest value) {
        this.getSPEnabledMarketsInformationRequest = value;
    }

}
