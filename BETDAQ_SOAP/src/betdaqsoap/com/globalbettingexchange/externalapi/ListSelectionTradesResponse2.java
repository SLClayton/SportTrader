
package com.globalbettingexchange.externalapi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ListSelectionTradesResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListSelectionTradesResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.GlobalBettingExchange.com/ExternalAPI/}BaseResponse"&gt;
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="SelectionTrades" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListSelectionTradesResponseItem"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListSelectionTradesResponse", propOrder = {
    "selectionTrades"
})
public class ListSelectionTradesResponse2
    extends BaseResponse
{

    @XmlElement(name = "SelectionTrades")
    protected List<ListSelectionTradesResponseItem> selectionTrades;

    /**
     * Gets the value of the selectionTrades property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the selectionTrades property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSelectionTrades().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ListSelectionTradesResponseItem }
     * 
     * 
     */
    public List<ListSelectionTradesResponseItem> getSelectionTrades() {
        if (selectionTrades == null) {
            selectionTrades = new ArrayList<ListSelectionTradesResponseItem>();
        }
        return this.selectionTrades;
    }

}
