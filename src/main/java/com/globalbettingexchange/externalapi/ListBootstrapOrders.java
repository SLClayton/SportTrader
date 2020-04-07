
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
 *         &lt;element name="listBootstrapOrdersRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListBootstrapOrdersRequest" minOccurs="0"/&gt;
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
    "listBootstrapOrdersRequest"
})
@XmlRootElement(name = "ListBootstrapOrders")
public class ListBootstrapOrders {

    protected ListBootstrapOrdersRequest listBootstrapOrdersRequest;

    /**
     * Gets the value of the listBootstrapOrdersRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ListBootstrapOrdersRequest }
     *     
     */
    public ListBootstrapOrdersRequest getListBootstrapOrdersRequest() {
        return listBootstrapOrdersRequest;
    }

    /**
     * Sets the value of the listBootstrapOrdersRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListBootstrapOrdersRequest }
     *     
     */
    public void setListBootstrapOrdersRequest(ListBootstrapOrdersRequest value) {
        this.listBootstrapOrdersRequest = value;
    }

}
