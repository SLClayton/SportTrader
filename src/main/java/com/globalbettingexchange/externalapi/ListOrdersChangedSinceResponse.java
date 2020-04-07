
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;


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
 *         &lt;element name="ListOrdersChangedSinceResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListOrdersChangedSinceResponse" minOccurs="0"/&gt;
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
    "listOrdersChangedSinceResult"
})
@XmlRootElement(name = "ListOrdersChangedSinceResponse")
public class ListOrdersChangedSinceResponse {

    @XmlElement(name = "ListOrdersChangedSinceResult")
    protected ListOrdersChangedSinceResponse2 listOrdersChangedSinceResult;

    /**
     * Gets the value of the listOrdersChangedSinceResult property.
     * 
     * @return
     *     possible object is
     *     {@link ListOrdersChangedSinceResponse2 }
     *     
     */
    public ListOrdersChangedSinceResponse2 getListOrdersChangedSinceResult() {
        return listOrdersChangedSinceResult;
    }

    /**
     * Sets the value of the listOrdersChangedSinceResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListOrdersChangedSinceResponse2 }
     *     
     */
    public void setListOrdersChangedSinceResult(ListOrdersChangedSinceResponse2 value) {
        this.listOrdersChangedSinceResult = value;
    }

}
