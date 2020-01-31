
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
 *         &lt;element name="suspendAllOrdersOnMarket" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}SuspendAllOrdersOnMarketRequest"/&gt;
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
    "suspendAllOrdersOnMarket"
})
@XmlRootElement(name = "SuspendAllOrdersOnMarket")
public class SuspendAllOrdersOnMarket {

    @XmlElement(required = true)
    protected SuspendAllOrdersOnMarketRequest suspendAllOrdersOnMarket;

    /**
     * Gets the value of the suspendAllOrdersOnMarket property.
     * 
     * @return
     *     possible object is
     *     {@link SuspendAllOrdersOnMarketRequest }
     *     
     */
    public SuspendAllOrdersOnMarketRequest getSuspendAllOrdersOnMarket() {
        return suspendAllOrdersOnMarket;
    }

    /**
     * Sets the value of the suspendAllOrdersOnMarket property.
     * 
     * @param value
     *     allowed object is
     *     {@link SuspendAllOrdersOnMarketRequest }
     *     
     */
    public void setSuspendAllOrdersOnMarket(SuspendAllOrdersOnMarketRequest value) {
        this.suspendAllOrdersOnMarket = value;
    }

}
