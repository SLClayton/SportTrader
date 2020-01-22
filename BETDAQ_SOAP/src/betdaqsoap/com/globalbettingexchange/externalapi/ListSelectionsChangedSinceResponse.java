
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
 *         &lt;element name="ListSelectionsChangedSinceResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListSelectionsChangedSinceResponse" minOccurs="0"/&gt;
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
    "listSelectionsChangedSinceResult"
})
@XmlRootElement(name = "ListSelectionsChangedSinceResponse")
public class ListSelectionsChangedSinceResponse {

    @XmlElement(name = "ListSelectionsChangedSinceResult")
    protected ListSelectionsChangedSinceResponse2 listSelectionsChangedSinceResult;

    /**
     * Gets the value of the listSelectionsChangedSinceResult property.
     * 
     * @return
     *     possible object is
     *     {@link ListSelectionsChangedSinceResponse2 }
     *     
     */
    public ListSelectionsChangedSinceResponse2 getListSelectionsChangedSinceResult() {
        return listSelectionsChangedSinceResult;
    }

    /**
     * Sets the value of the listSelectionsChangedSinceResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListSelectionsChangedSinceResponse2 }
     *     
     */
    public void setListSelectionsChangedSinceResult(ListSelectionsChangedSinceResponse2 value) {
        this.listSelectionsChangedSinceResult = value;
    }

}
