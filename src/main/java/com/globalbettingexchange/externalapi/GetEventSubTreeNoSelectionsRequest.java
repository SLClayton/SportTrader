
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for GetEventSubTreeNoSelectionsRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetEventSubTreeNoSelectionsRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;element name="EventClassifierIds" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="WantDirectDescendentsOnly" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="WantPlayMarkets" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetEventSubTreeNoSelectionsRequest", propOrder = {
    "eventClassifierIds"
})
public class GetEventSubTreeNoSelectionsRequest {

    @XmlElement(name = "EventClassifierIds", type = Long.class)
    protected List<Long> eventClassifierIds;
    @XmlAttribute(name = "WantDirectDescendentsOnly")
    protected Boolean wantDirectDescendentsOnly;
    @XmlAttribute(name = "WantPlayMarkets")
    protected Boolean wantPlayMarkets;

    /**
     * Gets the value of the eventClassifierIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the eventClassifierIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEventClassifierIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getEventClassifierIds() {
        if (eventClassifierIds == null) {
            eventClassifierIds = new ArrayList<Long>();
        }
        return this.eventClassifierIds;
    }

    /**
     * Gets the value of the wantDirectDescendentsOnly property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWantDirectDescendentsOnly() {
        return wantDirectDescendentsOnly;
    }

    /**
     * Sets the value of the wantDirectDescendentsOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWantDirectDescendentsOnly(Boolean value) {
        this.wantDirectDescendentsOnly = value;
    }

    /**
     * Gets the value of the wantPlayMarkets property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWantPlayMarkets() {
        return wantPlayMarkets;
    }

    /**
     * Sets the value of the wantPlayMarkets property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWantPlayMarkets(Boolean value) {
        this.wantPlayMarkets = value;
    }

}
