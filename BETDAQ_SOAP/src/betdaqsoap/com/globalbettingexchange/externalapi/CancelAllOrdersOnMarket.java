
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
 *         &lt;element name="cancelAllOrdersOnMarketRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}CancelAllOrdersOnMarketRequest" minOccurs="0"/&gt;
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
    "cancelAllOrdersOnMarketRequest"
})
@XmlRootElement(name = "CancelAllOrdersOnMarket")
public class CancelAllOrdersOnMarket {

    protected CancelAllOrdersOnMarketRequest cancelAllOrdersOnMarketRequest;

    /**
     * Gets the value of the cancelAllOrdersOnMarketRequest property.
     * 
     * @return
     *     possible object is
     *     {@link CancelAllOrdersOnMarketRequest }
     *     
     */
    public CancelAllOrdersOnMarketRequest getCancelAllOrdersOnMarketRequest() {
        return cancelAllOrdersOnMarketRequest;
    }

    /**
     * Sets the value of the cancelAllOrdersOnMarketRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link CancelAllOrdersOnMarketRequest }
     *     
     */
    public void setCancelAllOrdersOnMarketRequest(CancelAllOrdersOnMarketRequest value) {
        this.cancelAllOrdersOnMarketRequest = value;
    }

}
