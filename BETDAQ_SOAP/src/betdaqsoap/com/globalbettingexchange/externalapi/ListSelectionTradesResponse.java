
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
 *         &lt;element name="ListSelectionTradesResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListSelectionTradesResponse" minOccurs="0"/&gt;
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
    "listSelectionTradesResult"
})
@XmlRootElement(name = "ListSelectionTradesResponse")
public class ListSelectionTradesResponse {

    @XmlElement(name = "ListSelectionTradesResult")
    protected ListSelectionTradesResponse2 listSelectionTradesResult;

    /**
     * Gets the value of the listSelectionTradesResult property.
     * 
     * @return
     *     possible object is
     *     {@link ListSelectionTradesResponse2 }
     *     
     */
    public ListSelectionTradesResponse2 getListSelectionTradesResult() {
        return listSelectionTradesResult;
    }

    /**
     * Sets the value of the listSelectionTradesResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListSelectionTradesResponse2 }
     *     
     */
    public void setListSelectionTradesResult(ListSelectionTradesResponse2 value) {
        this.listSelectionTradesResult = value;
    }

}
