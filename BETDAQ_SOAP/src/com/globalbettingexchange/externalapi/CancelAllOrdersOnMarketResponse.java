
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
 *         &lt;element name="CancelAllOrdersOnMarketResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}CancelAllOrdersOnMarketResponse" minOccurs="0"/&gt;
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
    "cancelAllOrdersOnMarketResult"
})
@XmlRootElement(name = "CancelAllOrdersOnMarketResponse")
public class CancelAllOrdersOnMarketResponse {

    @XmlElement(name = "CancelAllOrdersOnMarketResult")
    protected CancelAllOrdersOnMarketResponse2 cancelAllOrdersOnMarketResult;

    /**
     * Gets the value of the cancelAllOrdersOnMarketResult property.
     * 
     * @return
     *     possible object is
     *     {@link CancelAllOrdersOnMarketResponse2 }
     *     
     */
    public CancelAllOrdersOnMarketResponse2 getCancelAllOrdersOnMarketResult() {
        return cancelAllOrdersOnMarketResult;
    }

    /**
     * Sets the value of the cancelAllOrdersOnMarketResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link CancelAllOrdersOnMarketResponse2 }
     *     
     */
    public void setCancelAllOrdersOnMarketResult(CancelAllOrdersOnMarketResponse2 value) {
        this.cancelAllOrdersOnMarketResult = value;
    }

}
