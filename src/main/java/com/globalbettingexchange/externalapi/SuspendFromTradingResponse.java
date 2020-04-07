
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
 *         &lt;element name="SuspendFromTradingResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}SuspendFromTradingResponse" minOccurs="0"/&gt;
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
    "suspendFromTradingResult"
})
@XmlRootElement(name = "SuspendFromTradingResponse")
public class SuspendFromTradingResponse {

    @XmlElement(name = "SuspendFromTradingResult")
    protected SuspendFromTradingResponse2 suspendFromTradingResult;

    /**
     * Gets the value of the suspendFromTradingResult property.
     * 
     * @return
     *     possible object is
     *     {@link SuspendFromTradingResponse2 }
     *     
     */
    public SuspendFromTradingResponse2 getSuspendFromTradingResult() {
        return suspendFromTradingResult;
    }

    /**
     * Sets the value of the suspendFromTradingResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link SuspendFromTradingResponse2 }
     *     
     */
    public void setSuspendFromTradingResult(SuspendFromTradingResponse2 value) {
        this.suspendFromTradingResult = value;
    }

}
