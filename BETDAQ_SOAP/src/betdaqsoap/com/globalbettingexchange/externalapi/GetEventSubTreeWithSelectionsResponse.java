
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
 *         &lt;element name="GetEventSubTreeWithSelectionsResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetEventSubTreeWithSelectionsResponse" minOccurs="0"/&gt;
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
    "getEventSubTreeWithSelectionsResult"
})
@XmlRootElement(name = "GetEventSubTreeWithSelectionsResponse")
public class GetEventSubTreeWithSelectionsResponse {

    @XmlElement(name = "GetEventSubTreeWithSelectionsResult")
    protected GetEventSubTreeWithSelectionsResponse2 getEventSubTreeWithSelectionsResult;

    /**
     * Gets the value of the getEventSubTreeWithSelectionsResult property.
     * 
     * @return
     *     possible object is
     *     {@link GetEventSubTreeWithSelectionsResponse2 }
     *     
     */
    public GetEventSubTreeWithSelectionsResponse2 getGetEventSubTreeWithSelectionsResult() {
        return getEventSubTreeWithSelectionsResult;
    }

    /**
     * Sets the value of the getEventSubTreeWithSelectionsResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetEventSubTreeWithSelectionsResponse2 }
     *     
     */
    public void setGetEventSubTreeWithSelectionsResult(GetEventSubTreeWithSelectionsResponse2 value) {
        this.getEventSubTreeWithSelectionsResult = value;
    }

}
