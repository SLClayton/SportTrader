
package com.globalbettingexchange.externalapi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ListSelectionsChangedSinceResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListSelectionsChangedSinceResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.GlobalBettingExchange.com/ExternalAPI/}BaseResponse"&gt;
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="Selections" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListSelectionsChangedSinceResponseItem"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListSelectionsChangedSinceResponse", propOrder = {
    "selections"
})
public class ListSelectionsChangedSinceResponse2
    extends BaseResponse
{

    @XmlElement(name = "Selections")
    protected List<ListSelectionsChangedSinceResponseItem> selections;

    /**
     * Gets the value of the selections property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the selections property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSelections().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ListSelectionsChangedSinceResponseItem }
     * 
     * 
     */
    public List<ListSelectionsChangedSinceResponseItem> getSelections() {
        if (selections == null) {
            selections = new ArrayList<ListSelectionsChangedSinceResponseItem>();
        }
        return this.selections;
    }

}
