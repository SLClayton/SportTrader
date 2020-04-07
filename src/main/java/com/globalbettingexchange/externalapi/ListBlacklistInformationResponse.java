
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;


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
 *         &lt;element name="ListBlacklistInformationResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListBlacklistInformationResponse" minOccurs="0"/&gt;
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
    "listBlacklistInformationResult"
})
@XmlRootElement(name = "ListBlacklistInformationResponse")
public class ListBlacklistInformationResponse {

    @XmlElement(name = "ListBlacklistInformationResult")
    protected ListBlacklistInformationResponse2 listBlacklistInformationResult;

    /**
     * Gets the value of the listBlacklistInformationResult property.
     * 
     * @return
     *     possible object is
     *     {@link ListBlacklistInformationResponse2 }
     *     
     */
    public ListBlacklistInformationResponse2 getListBlacklistInformationResult() {
        return listBlacklistInformationResult;
    }

    /**
     * Sets the value of the listBlacklistInformationResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListBlacklistInformationResponse2 }
     *     
     */
    public void setListBlacklistInformationResult(ListBlacklistInformationResponse2 value) {
        this.listBlacklistInformationResult = value;
    }

}
