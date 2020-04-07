
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
 *         &lt;element name="listTopLevelEventsRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListTopLevelEventsRequest" minOccurs="0"/&gt;
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
    "listTopLevelEventsRequest"
})
@XmlRootElement(name = "ListTopLevelEvents")
public class ListTopLevelEvents {

    protected ListTopLevelEventsRequest listTopLevelEventsRequest;

    /**
     * Gets the value of the listTopLevelEventsRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ListTopLevelEventsRequest }
     *     
     */
    public ListTopLevelEventsRequest getListTopLevelEventsRequest() {
        return listTopLevelEventsRequest;
    }

    /**
     * Sets the value of the listTopLevelEventsRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListTopLevelEventsRequest }
     *     
     */
    public void setListTopLevelEventsRequest(ListTopLevelEventsRequest value) {
        this.listTopLevelEventsRequest = value;
    }

}
