
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
 *         &lt;element name="ListTaggedValuesResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListTaggedValuesResponse" minOccurs="0"/&gt;
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
    "listTaggedValuesResult"
})
@XmlRootElement(name = "ListTaggedValuesResponse")
public class ListTaggedValuesResponse {

    @XmlElement(name = "ListTaggedValuesResult")
    protected ListTaggedValuesResponse2 listTaggedValuesResult;

    /**
     * Gets the value of the listTaggedValuesResult property.
     * 
     * @return
     *     possible object is
     *     {@link ListTaggedValuesResponse2 }
     *     
     */
    public ListTaggedValuesResponse2 getListTaggedValuesResult() {
        return listTaggedValuesResult;
    }

    /**
     * Sets the value of the listTaggedValuesResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListTaggedValuesResponse2 }
     *     
     */
    public void setListTaggedValuesResult(ListTaggedValuesResponse2 value) {
        this.listTaggedValuesResult = value;
    }

}
