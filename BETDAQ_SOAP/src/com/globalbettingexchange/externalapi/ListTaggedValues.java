
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
 *         &lt;element name="listTaggedValuesRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListTaggedValuesRequest" minOccurs="0"/&gt;
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
    "listTaggedValuesRequest"
})
@XmlRootElement(name = "ListTaggedValues")
public class ListTaggedValues {

    protected ListTaggedValuesRequest listTaggedValuesRequest;

    /**
     * Gets the value of the listTaggedValuesRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ListTaggedValuesRequest }
     *     
     */
    public ListTaggedValuesRequest getListTaggedValuesRequest() {
        return listTaggedValuesRequest;
    }

    /**
     * Sets the value of the listTaggedValuesRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListTaggedValuesRequest }
     *     
     */
    public void setListTaggedValuesRequest(ListTaggedValuesRequest value) {
        this.listTaggedValuesRequest = value;
    }

}
