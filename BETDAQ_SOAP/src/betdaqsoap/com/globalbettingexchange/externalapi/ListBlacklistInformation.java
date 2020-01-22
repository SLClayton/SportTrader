
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
 *         &lt;element name="listBlacklistInformationRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListBlacklistInformationRequest" minOccurs="0"/&gt;
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
    "listBlacklistInformationRequest"
})
@XmlRootElement(name = "ListBlacklistInformation")
public class ListBlacklistInformation {

    protected ListBlacklistInformationRequest listBlacklistInformationRequest;

    /**
     * Gets the value of the listBlacklistInformationRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ListBlacklistInformationRequest }
     *     
     */
    public ListBlacklistInformationRequest getListBlacklistInformationRequest() {
        return listBlacklistInformationRequest;
    }

    /**
     * Sets the value of the listBlacklistInformationRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListBlacklistInformationRequest }
     *     
     */
    public void setListBlacklistInformationRequest(ListBlacklistInformationRequest value) {
        this.listBlacklistInformationRequest = value;
    }

}
