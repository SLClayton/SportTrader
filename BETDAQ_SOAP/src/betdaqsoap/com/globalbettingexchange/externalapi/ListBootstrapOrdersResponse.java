
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
 *         &lt;element name="ListBootstrapOrdersResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListBootstrapOrdersResponse" minOccurs="0"/&gt;
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
    "listBootstrapOrdersResult"
})
@XmlRootElement(name = "ListBootstrapOrdersResponse")
public class ListBootstrapOrdersResponse {

    @XmlElement(name = "ListBootstrapOrdersResult")
    protected ListBootstrapOrdersResponse2 listBootstrapOrdersResult;

    /**
     * Gets the value of the listBootstrapOrdersResult property.
     * 
     * @return
     *     possible object is
     *     {@link ListBootstrapOrdersResponse2 }
     *     
     */
    public ListBootstrapOrdersResponse2 getListBootstrapOrdersResult() {
        return listBootstrapOrdersResult;
    }

    /**
     * Sets the value of the listBootstrapOrdersResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListBootstrapOrdersResponse2 }
     *     
     */
    public void setListBootstrapOrdersResult(ListBootstrapOrdersResponse2 value) {
        this.listBootstrapOrdersResult = value;
    }

}
