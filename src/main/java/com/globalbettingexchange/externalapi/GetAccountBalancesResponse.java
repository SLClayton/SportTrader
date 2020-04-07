
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
 *         &lt;element name="GetAccountBalancesResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetAccountBalancesResponse" minOccurs="0"/&gt;
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
    "getAccountBalancesResult"
})
@XmlRootElement(name = "GetAccountBalancesResponse")
public class GetAccountBalancesResponse {

    @XmlElement(name = "GetAccountBalancesResult")
    protected GetAccountBalancesResponse2 getAccountBalancesResult;

    /**
     * Gets the value of the getAccountBalancesResult property.
     * 
     * @return
     *     possible object is
     *     {@link GetAccountBalancesResponse2 }
     *     
     */
    public GetAccountBalancesResponse2 getGetAccountBalancesResult() {
        return getAccountBalancesResult;
    }

    /**
     * Sets the value of the getAccountBalancesResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetAccountBalancesResponse2 }
     *     
     */
    public void setGetAccountBalancesResult(GetAccountBalancesResponse2 value) {
        this.getAccountBalancesResult = value;
    }

}
