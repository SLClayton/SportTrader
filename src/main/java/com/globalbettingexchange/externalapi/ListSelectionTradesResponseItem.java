
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ListSelectionTradesResponseItem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListSelectionTradesResponseItem"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="TradeItems" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}TradeItemType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="selectionId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="maxTradeId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="maxTradeIdReturned" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListSelectionTradesResponseItem", propOrder = {
    "tradeItems"
})
public class ListSelectionTradesResponseItem {

    @XmlElement(name = "TradeItems")
    protected List<TradeItemType> tradeItems;
    @XmlAttribute(name = "selectionId", required = true)
    protected long selectionId;
    @XmlAttribute(name = "maxTradeId", required = true)
    protected long maxTradeId;
    @XmlAttribute(name = "maxTradeIdReturned", required = true)
    protected long maxTradeIdReturned;

    /**
     * Gets the value of the tradeItems property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tradeItems property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTradeItems().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TradeItemType }
     * 
     * 
     */
    public List<TradeItemType> getTradeItems() {
        if (tradeItems == null) {
            tradeItems = new ArrayList<TradeItemType>();
        }
        return this.tradeItems;
    }

    /**
     * Gets the value of the selectionId property.
     * 
     */
    public long getSelectionId() {
        return selectionId;
    }

    /**
     * Sets the value of the selectionId property.
     * 
     */
    public void setSelectionId(long value) {
        this.selectionId = value;
    }

    /**
     * Gets the value of the maxTradeId property.
     * 
     */
    public long getMaxTradeId() {
        return maxTradeId;
    }

    /**
     * Sets the value of the maxTradeId property.
     * 
     */
    public void setMaxTradeId(long value) {
        this.maxTradeId = value;
    }

    /**
     * Gets the value of the maxTradeIdReturned property.
     * 
     */
    public long getMaxTradeIdReturned() {
        return maxTradeIdReturned;
    }

    /**
     * Sets the value of the maxTradeIdReturned property.
     * 
     */
    public void setMaxTradeIdReturned(long value) {
        this.maxTradeIdReturned = value;
    }

}
