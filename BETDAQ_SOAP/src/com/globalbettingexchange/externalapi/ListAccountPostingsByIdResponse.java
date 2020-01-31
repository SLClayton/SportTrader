
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
 *         &lt;element name="ListAccountPostingsByIdResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListAccountPostingsByIdResponse" minOccurs="0"/&gt;
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
    "listAccountPostingsByIdResult"
})
@XmlRootElement(name = "ListAccountPostingsByIdResponse")
public class ListAccountPostingsByIdResponse {

    @XmlElement(name = "ListAccountPostingsByIdResult")
    protected ListAccountPostingsByIdResponse2 listAccountPostingsByIdResult;

    /**
     * Gets the value of the listAccountPostingsByIdResult property.
     * 
     * @return
     *     possible object is
     *     {@link ListAccountPostingsByIdResponse2 }
     *     
     */
    public ListAccountPostingsByIdResponse2 getListAccountPostingsByIdResult() {
        return listAccountPostingsByIdResult;
    }

    /**
     * Sets the value of the listAccountPostingsByIdResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListAccountPostingsByIdResponse2 }
     *     
     */
    public void setListAccountPostingsByIdResult(ListAccountPostingsByIdResponse2 value) {
        this.listAccountPostingsByIdResult = value;
    }

}
