
package com.globalbettingexchange.externalapi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ListSelectionTradesRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListSelectionTradesRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;element name="selectionRequests" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}SelectionTradesRequestItem"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="currency" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListSelectionTradesRequest", propOrder = {
    "selectionRequests"
})
public class ListSelectionTradesRequest {

    @XmlElement(required = true)
    protected List<SelectionTradesRequestItem> selectionRequests;
    @XmlAttribute(name = "currency", required = true)
    protected String currency;

    /**
     * Gets the value of the selectionRequests property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the selectionRequests property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSelectionRequests().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SelectionTradesRequestItem }
     * 
     * 
     */
    public List<SelectionTradesRequestItem> getSelectionRequests() {
        if (selectionRequests == null) {
            selectionRequests = new ArrayList<SelectionTradesRequestItem>();
        }
        return this.selectionRequests;
    }

    /**
     * Gets the value of the currency property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the value of the currency property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrency(String value) {
        this.currency = value;
    }

}
