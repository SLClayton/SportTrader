
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ListBootstrapOrdersResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListBootstrapOrdersResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.GlobalBettingExchange.com/ExternalAPI/}BaseResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Orders"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Order" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}Order" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="MaximumSequenceNumber" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListBootstrapOrdersResponse", propOrder = {
    "orders"
})
public class ListBootstrapOrdersResponse2
    extends BaseResponse
{

    @XmlElement(name = "Orders", required = true)
    protected Orders orders;
    @XmlAttribute(name = "MaximumSequenceNumber", required = true)
    protected long maximumSequenceNumber;

    /**
     * Gets the value of the orders property.
     *
     * @return
     *     possible object is
     *     {@link ListBootstrapOrdersResponse2 .Orders }
     *
     */
    public Orders getOrders() {
        return orders;
    }

    /**
     * Sets the value of the orders property.
     *
     * @param value
     *     allowed object is
     *     {@link ListBootstrapOrdersResponse2 .Orders }
     *
     */
    public void setOrders(Orders value) {
        this.orders = value;
    }

    /**
     * Gets the value of the maximumSequenceNumber property.
     * 
     */
    public long getMaximumSequenceNumber() {
        return maximumSequenceNumber;
    }

    /**
     * Sets the value of the maximumSequenceNumber property.
     * 
     */
    public void setMaximumSequenceNumber(long value) {
        this.maximumSequenceNumber = value;
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
     *         &lt;element name="Order" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}Order" maxOccurs="unbounded"/&gt;
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
        protected List<Order> order;

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
         * {@link Order }
         * 
         * 
         */
        public List<Order> getOrder() {
            if (order == null) {
                order = new ArrayList<Order>();
            }
            return this.order;
        }

    }

}
