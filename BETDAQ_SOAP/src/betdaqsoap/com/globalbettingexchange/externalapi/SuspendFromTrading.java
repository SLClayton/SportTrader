
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
 *         &lt;element name="suspendFromTradingRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}SuspendFromTradingRequest" minOccurs="0"/&gt;
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
    "suspendFromTradingRequest"
})
@XmlRootElement(name = "SuspendFromTrading")
public class SuspendFromTrading {

    protected SuspendFromTradingRequest suspendFromTradingRequest;

    /**
     * Gets the value of the suspendFromTradingRequest property.
     * 
     * @return
     *     possible object is
     *     {@link SuspendFromTradingRequest }
     *     
     */
    public SuspendFromTradingRequest getSuspendFromTradingRequest() {
        return suspendFromTradingRequest;
    }

    /**
     * Sets the value of the suspendFromTradingRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link SuspendFromTradingRequest }
     *     
     */
    public void setSuspendFromTradingRequest(SuspendFromTradingRequest value) {
        this.suspendFromTradingRequest = value;
    }

}
