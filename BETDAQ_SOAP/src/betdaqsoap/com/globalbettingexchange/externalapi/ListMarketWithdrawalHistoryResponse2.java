
package com.globalbettingexchange.externalapi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ListMarketWithdrawalHistoryResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListMarketWithdrawalHistoryResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.GlobalBettingExchange.com/ExternalAPI/}BaseResponse"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;element name="Withdrawals" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListMarketWithdrawalHistoryResponseItem"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListMarketWithdrawalHistoryResponse", propOrder = {
    "withdrawals"
})
public class ListMarketWithdrawalHistoryResponse2
    extends BaseResponse
{

    @XmlElement(name = "Withdrawals", required = true)
    protected List<ListMarketWithdrawalHistoryResponseItem> withdrawals;

    /**
     * Gets the value of the withdrawals property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the withdrawals property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWithdrawals().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ListMarketWithdrawalHistoryResponseItem }
     * 
     * 
     */
    public List<ListMarketWithdrawalHistoryResponseItem> getWithdrawals() {
        if (withdrawals == null) {
            withdrawals = new ArrayList<ListMarketWithdrawalHistoryResponseItem>();
        }
        return this.withdrawals;
    }

}
