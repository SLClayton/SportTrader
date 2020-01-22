
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
 *         &lt;element name="listOrdersChangedSinceRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListOrdersChangedSinceRequest" minOccurs="0"/&gt;
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
    "listOrdersChangedSinceRequest"
})
@XmlRootElement(name = "ListOrdersChangedSince")
public class ListOrdersChangedSince {

    protected ListOrdersChangedSinceRequest listOrdersChangedSinceRequest;

    /**
     * Gets the value of the listOrdersChangedSinceRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ListOrdersChangedSinceRequest }
     *     
     */
    public ListOrdersChangedSinceRequest getListOrdersChangedSinceRequest() {
        return listOrdersChangedSinceRequest;
    }

    /**
     * Sets the value of the listOrdersChangedSinceRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListOrdersChangedSinceRequest }
     *     
     */
    public void setListOrdersChangedSinceRequest(ListOrdersChangedSinceRequest value) {
        this.listOrdersChangedSinceRequest = value;
    }

}
