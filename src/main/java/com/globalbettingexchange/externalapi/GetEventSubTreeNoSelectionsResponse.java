
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
 *         &lt;element name="GetEventSubTreeNoSelectionsResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetEventSubTreeNoSelectionsResponse" minOccurs="0"/&gt;
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
    "getEventSubTreeNoSelectionsResult"
})
@XmlRootElement(name = "GetEventSubTreeNoSelectionsResponse")
public class GetEventSubTreeNoSelectionsResponse {

    @XmlElement(name = "GetEventSubTreeNoSelectionsResult")
    protected GetEventSubTreeNoSelectionsResponse2 getEventSubTreeNoSelectionsResult;

    /**
     * Gets the value of the getEventSubTreeNoSelectionsResult property.
     * 
     * @return
     *     possible object is
     *     {@link GetEventSubTreeNoSelectionsResponse2 }
     *     
     */
    public GetEventSubTreeNoSelectionsResponse2 getGetEventSubTreeNoSelectionsResult() {
        return getEventSubTreeNoSelectionsResult;
    }

    /**
     * Sets the value of the getEventSubTreeNoSelectionsResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetEventSubTreeNoSelectionsResponse2 }
     *     
     */
    public void setGetEventSubTreeNoSelectionsResult(GetEventSubTreeNoSelectionsResponse2 value) {
        this.getEventSubTreeNoSelectionsResult = value;
    }

}
