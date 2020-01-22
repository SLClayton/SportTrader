
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="ListTopLevelEventsResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListTopLevelEventsResponse" minOccurs="0"/&gt;
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
    "listTopLevelEventsResult"
})
@XmlRootElement(name = "ListTopLevelEventsResponse")
public class ListTopLevelEventsResponse {

    @XmlElement(name = "ListTopLevelEventsResult")
    protected ListTopLevelEventsResponse2 listTopLevelEventsResult;

    /**
     * Gets the value of the listTopLevelEventsResult property.
     * 
     * @return
     *     possible object is
     *     {@link ListTopLevelEventsResponse2 }
     *     
     */
    public ListTopLevelEventsResponse2 getListTopLevelEventsResult() {
        return listTopLevelEventsResult;
    }

    /**
     * Sets the value of the listTopLevelEventsResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListTopLevelEventsResponse2 }
     *     
     */
    public void setListTopLevelEventsResult(ListTopLevelEventsResponse2 value) {
        this.listTopLevelEventsResult = value;
    }

}
