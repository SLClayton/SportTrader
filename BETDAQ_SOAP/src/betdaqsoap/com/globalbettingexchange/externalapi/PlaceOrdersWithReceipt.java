
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
 *         &lt;element name="orders" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}PlaceOrdersWithReceiptRequest" minOccurs="0"/&gt;
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
    "orders"
})
@XmlRootElement(name = "PlaceOrdersWithReceipt")
public class PlaceOrdersWithReceipt {

    protected PlaceOrdersWithReceiptRequest orders;

    /**
     * Gets the value of the orders property.
     * 
     * @return
     *     possible object is
     *     {@link PlaceOrdersWithReceiptRequest }
     *     
     */
    public PlaceOrdersWithReceiptRequest getOrders() {
        return orders;
    }

    /**
     * Sets the value of the orders property.
     * 
     * @param value
     *     allowed object is
     *     {@link PlaceOrdersWithReceiptRequest }
     *     
     */
    public void setOrders(PlaceOrdersWithReceiptRequest value) {
        this.orders = value;
    }

}
