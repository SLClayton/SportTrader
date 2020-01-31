
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
 *         &lt;element name="ListAccountPostingsResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListAccountPostingsResponse" minOccurs="0"/&gt;
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
    "listAccountPostingsResult"
})
@XmlRootElement(name = "ListAccountPostingsResponse")
public class ListAccountPostingsResponse {

    @XmlElement(name = "ListAccountPostingsResult")
    protected ListAccountPostingsResponse2 listAccountPostingsResult;

    /**
     * Gets the value of the listAccountPostingsResult property.
     * 
     * @return
     *     possible object is
     *     {@link ListAccountPostingsResponse2 }
     *     
     */
    public ListAccountPostingsResponse2 getListAccountPostingsResult() {
        return listAccountPostingsResult;
    }

    /**
     * Sets the value of the listAccountPostingsResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListAccountPostingsResponse2 }
     *     
     */
    public void setListAccountPostingsResult(ListAccountPostingsResponse2 value) {
        this.listAccountPostingsResult = value;
    }

}
