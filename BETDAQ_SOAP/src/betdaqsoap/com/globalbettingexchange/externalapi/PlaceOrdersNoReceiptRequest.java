
package com.globalbettingexchange.externalapi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PlaceOrdersNoReceiptRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PlaceOrdersNoReceiptRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Orders"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Order" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}SimpleOrderRequest" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="WantAllOrNothingBehaviour" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlaceOrdersNoReceiptRequest", propOrder = {
    "orders",
    "wantAllOrNothingBehaviour"
})
public class PlaceOrdersNoReceiptRequest {

    @XmlElement(name = "Orders", required = true)
    protected PlaceOrdersNoReceiptRequest.Orders orders;
    @XmlElement(name = "WantAllOrNothingBehaviour")
    protected boolean wantAllOrNothingBehaviour;

    /**
     * Gets the value of the orders property.
     * 
     * @return
     *     possible object is
     *     {@link PlaceOrdersNoReceiptRequest.Orders }
     *     
     */
    public PlaceOrdersNoReceiptRequest.Orders getOrders() {
        return orders;
    }

    /**
     * Sets the value of the orders property.
     * 
     * @param value
     *     allowed object is
     *     {@link PlaceOrdersNoReceiptRequest.Orders }
     *     
     */
    public void setOrders(PlaceOrdersNoReceiptRequest.Orders value) {
        this.orders = value;
    }

    /**
     * Gets the value of the wantAllOrNothingBehaviour property.
     * 
     */
    public boolean isWantAllOrNothingBehaviour() {
        return wantAllOrNothingBehaviour;
    }

    /**
     * Sets the value of the wantAllOrNothingBehaviour property.
     * 
     */
    public void setWantAllOrNothingBehaviour(boolean value) {
        this.wantAllOrNothingBehaviour = value;
    }


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
     *         &lt;element name="Order" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}SimpleOrderRequest" maxOccurs="unbounded"/&gt;
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
        "order"
    })
    public static class Orders {

        @XmlElement(name = "Order", required = true)
        protected List<SimpleOrderRequest> order;

        /**
         * Gets the value of the order property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the order property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getOrder().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link SimpleOrderRequest }
         * 
         * 
         */
        public List<SimpleOrderRequest> getOrder() {
            if (order == null) {
                order = new ArrayList<SimpleOrderRequest>();
            }
            return this.order;
        }

    }

}
