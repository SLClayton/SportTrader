
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for CancelAllOrdersOnMarketResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CancelAllOrdersOnMarketResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.GlobalBettingExchange.com/ExternalAPI/}BaseResponse"&gt;
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="OrderIds" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="Order" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}CancelAllOrdersOnMarketResponseItem"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CancelAllOrdersOnMarketResponse", propOrder = {
    "orderIdsAndOrder"
})
public class CancelAllOrdersOnMarketResponse2
    extends BaseResponse
{

    @XmlElements({
        @XmlElement(name = "OrderIds", type = Long.class),
        @XmlElement(name = "Order", type = CancelAllOrdersOnMarketResponseItem.class)
    })
    protected List<Object> orderIdsAndOrder;

    /**
     * Gets the value of the orderIdsAndOrder property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the orderIdsAndOrder property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOrderIdsAndOrder().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * {@link CancelAllOrdersOnMarketResponseItem }
     * 
     * 
     */
    public List<Object> getOrderIdsAndOrder() {
        if (orderIdsAndOrder == null) {
            orderIdsAndOrder = new ArrayList<Object>();
        }
        return this.orderIdsAndOrder;
    }

}
