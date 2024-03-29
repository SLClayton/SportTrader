
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
 *         &lt;element name="getCurrentSelectionSequenceNumberRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetCurrentSelectionSequenceNumberRequest" minOccurs="0"/&gt;
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
    "getCurrentSelectionSequenceNumberRequest"
})
@XmlRootElement(name = "GetCurrentSelectionSequenceNumber")
public class GetCurrentSelectionSequenceNumber {

    protected GetCurrentSelectionSequenceNumberRequest getCurrentSelectionSequenceNumberRequest;

    /**
     * Gets the value of the getCurrentSelectionSequenceNumberRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetCurrentSelectionSequenceNumberRequest }
     *     
     */
    public GetCurrentSelectionSequenceNumberRequest getGetCurrentSelectionSequenceNumberRequest() {
        return getCurrentSelectionSequenceNumberRequest;
    }

    /**
     * Sets the value of the getCurrentSelectionSequenceNumberRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetCurrentSelectionSequenceNumberRequest }
     *     
     */
    public void setGetCurrentSelectionSequenceNumberRequest(GetCurrentSelectionSequenceNumberRequest value) {
        this.getCurrentSelectionSequenceNumberRequest = value;
    }

}
