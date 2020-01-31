
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
 *         &lt;element name="SuspendAllOrdersOnMarketResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}SuspendAllOrdersOnMarketResponse" minOccurs="0"/&gt;
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
    "suspendAllOrdersOnMarketResult"
})
@XmlRootElement(name = "SuspendAllOrdersOnMarketResponse")
public class SuspendAllOrdersOnMarketResponse {

    @XmlElement(name = "SuspendAllOrdersOnMarketResult")
    protected SuspendAllOrdersOnMarketResponse2 suspendAllOrdersOnMarketResult;

    /**
     * Gets the value of the suspendAllOrdersOnMarketResult property.
     * 
     * @return
     *     possible object is
     *     {@link SuspendAllOrdersOnMarketResponse2 }
     *     
     */
    public SuspendAllOrdersOnMarketResponse2 getSuspendAllOrdersOnMarketResult() {
        return suspendAllOrdersOnMarketResult;
    }

    /**
     * Sets the value of the suspendAllOrdersOnMarketResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link SuspendAllOrdersOnMarketResponse2 }
     *     
     */
    public void setSuspendAllOrdersOnMarketResult(SuspendAllOrdersOnMarketResponse2 value) {
        this.suspendAllOrdersOnMarketResult = value;
    }

}
