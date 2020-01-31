
package com.globalbettingexchange.externalapi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CancelOrdersRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CancelOrdersRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;sequence maxOccurs="unbounded"&gt;
 *           &lt;element name="OrderHandle" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;/sequence&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CancelOrdersRequest", propOrder = {
    "orderHandle"
})
public class CancelOrdersRequest {

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
