
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
 *         &lt;element name="UnsuspendFromTradingResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}UnsuspendFromTradingResponse" minOccurs="0"/&gt;
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
    "unsuspendFromTradingResult"
})
@XmlRootElement(name = "UnsuspendFromTradingResponse")
public class UnsuspendFromTradingResponse {

    @XmlElement(name = "UnsuspendFromTradingResult")
    protected UnsuspendFromTradingResponse2 unsuspendFromTradingResult;

    /**
     * Gets the value of the unsuspendFromTradingResult property.
     * 
     * @return
     *     possible object is
     *     {@link UnsuspendFromTradingResponse2 }
     *     
     */
    public UnsuspendFromTradingResponse2 getUnsuspendFromTradingResult() {
        return unsuspendFromTradingResult;
    }

    /**
     * Sets the value of the unsuspendFromTradingResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnsuspendFromTradingResponse2 }
     *     
     */
    public void setUnsuspendFromTradingResult(UnsuspendFromTradingResponse2 value) {
        this.unsuspendFromTradingResult = value;
    }

}
