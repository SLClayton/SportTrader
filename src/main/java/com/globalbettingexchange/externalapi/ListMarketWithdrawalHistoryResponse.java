
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
 *         &lt;element name="ListMarketWithdrawalHistoryResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListMarketWithdrawalHistoryResponse" minOccurs="0"/&gt;
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
    "listMarketWithdrawalHistoryResult"
})
@XmlRootElement(name = "ListMarketWithdrawalHistoryResponse")
public class ListMarketWithdrawalHistoryResponse {

    @XmlElement(name = "ListMarketWithdrawalHistoryResult")
    protected ListMarketWithdrawalHistoryResponse2 listMarketWithdrawalHistoryResult;

    /**
     * Gets the value of the listMarketWithdrawalHistoryResult property.
     * 
     * @return
     *     possible object is
     *     {@link ListMarketWithdrawalHistoryResponse2 }
     *     
     */
    public ListMarketWithdrawalHistoryResponse2 getListMarketWithdrawalHistoryResult() {
        return listMarketWithdrawalHistoryResult;
    }

    /**
     * Sets the value of the listMarketWithdrawalHistoryResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListMarketWithdrawalHistoryResponse2 }
     *     
     */
    public void setListMarketWithdrawalHistoryResult(ListMarketWithdrawalHistoryResponse2 value) {
        this.listMarketWithdrawalHistoryResult = value;
    }

}
