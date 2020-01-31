
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
 *         &lt;element name="getEventSubTreeWithSelectionsRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetEventSubTreeWithSelectionsRequest" minOccurs="0"/&gt;
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
    "getEventSubTreeWithSelectionsRequest"
})
@XmlRootElement(name = "GetEventSubTreeWithSelections")
public class GetEventSubTreeWithSelections {

    protected GetEventSubTreeWithSelectionsRequest getEventSubTreeWithSelectionsRequest;

    /**
     * Gets the value of the getEventSubTreeWithSelectionsRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetEventSubTreeWithSelectionsRequest }
     *     
     */
    public GetEventSubTreeWithSelectionsRequest getGetEventSubTreeWithSelectionsRequest() {
        return getEventSubTreeWithSelectionsRequest;
    }

    /**
     * Sets the value of the getEventSubTreeWithSelectionsRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetEventSubTreeWithSelectionsRequest }
     *     
     */
    public void setGetEventSubTreeWithSelectionsRequest(GetEventSubTreeWithSelectionsRequest value) {
        this.getEventSubTreeWithSelectionsRequest = value;
    }

}
