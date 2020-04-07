
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
 *         &lt;element name="listAccountPostingsByIdRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListAccountPostingsByIdRequest" minOccurs="0"/&gt;
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
    "listAccountPostingsByIdRequest"
})
@XmlRootElement(name = "ListAccountPostingsById")
public class ListAccountPostingsById {

    protected ListAccountPostingsByIdRequest listAccountPostingsByIdRequest;

    /**
     * Gets the value of the listAccountPostingsByIdRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ListAccountPostingsByIdRequest }
     *     
     */
    public ListAccountPostingsByIdRequest getListAccountPostingsByIdRequest() {
        return listAccountPostingsByIdRequest;
    }

    /**
     * Sets the value of the listAccountPostingsByIdRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListAccountPostingsByIdRequest }
     *     
     */
    public void setListAccountPostingsByIdRequest(ListAccountPostingsByIdRequest value) {
        this.listAccountPostingsByIdRequest = value;
    }

}
