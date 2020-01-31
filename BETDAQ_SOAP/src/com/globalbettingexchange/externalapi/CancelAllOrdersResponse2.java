
package com.globalbettingexchange.externalapi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CancelAllOrdersResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CancelAllOrdersResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.GlobalBettingExchange.com/ExternalAPI/}BaseResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="CancelledOrdersHandles"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="OrderHandle" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="Orders"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Order" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}CancelAllOrdersResponseItem" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CancelAllOrdersResponse", propOrder = {
    "cancelledOrdersHandles",
    "orders"
})
public class CancelAllOrdersResponse2
    extends BaseResponse
{

    @XmlElement(name = "CancelledOrdersHandles", required = true)
    protected CancelAllOrdersResponse2 .CancelledOrdersHandles cancelledOrdersHandles;
    @XmlElement(name = "Orders", required = true)
    protected CancelAllOrdersResponse2 .Orders orders;

    /**
     * Gets the value of the cancelledOrdersHandles property.
     * 
     * @return
     *     possible object is
     *     {@link CancelAllOrdersResponse2 .CancelledOrdersHandles }
     *     
     */
    public CancelAllOrdersResponse2 .CancelledOrdersHandles getCancelledOrdersHandles() {
        return cancelledOrdersHandles;
    }

    /**
     * Sets the value of the cancelledOrdersHandles property.
     * 
     * @param value
     *     allowed object is
     *     {@link CancelAllOrdersResponse2 .CancelledOrdersHandles }
     *     
     */
    public void setCancelledOrdersHandles(CancelAllOrdersResponse2 .CancelledOrdersHandles value) {
        this.cancelledOrdersHandles = value;
    }

    /**
     * Gets the value of the orders property.
     * 
     * @return
     *     possible object is
     *     {@link CancelAllOrdersResponse2 .Orders }
     *     
     */
    public CancelAllOrdersResponse2 .Orders getOrders() {
        return orders;
    }

    /**
     * Sets the value of the orders property.
     * 
     * @param value
     *     allowed object is
     *     {@link CancelAllOrdersResponse2 .Orders }
     *     
     */
    public void setOrders(CancelAllOrdersResponse2 .Orders value) {
        this.orders = value;
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
     *         &lt;element name="OrderHandle" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded"/&gt;
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
        "orderHandle"
    })
    public static class CancelledOrdersHandles {

        @XmlElement(name = "OrderHandle", type = Long.class)
        protected List<Long> orderHandle;

        /**
         * Gets the value of the orderHandle property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the orderHandle property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getOrderHandle().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Long }
         * 
         * 
         */
        public List<Long> getOrderHandle() {
            if (orderHandle == null) {
                orderHandle = new ArrayList<Long>();
            }
            return this.orderHandle;
        }

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
     *         &lt;element name="Order" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}CancelAllOrdersResponseItem" maxOccurs="unbounded"/&gt;
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
        protected List<CancelAllOrdersResponseItem> order;

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
         * {@link CancelAllOrdersResponseItem }
         * 
         * 
         */
        public List<CancelAllOrdersResponseItem> getOrder() {
            if (order == null) {
                order = new ArrayList<CancelAllOrdersResponseItem>();
            }
            return this.order;
        }

    }

}
