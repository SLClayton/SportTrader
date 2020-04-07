
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
 *         &lt;element name="GetCurrentSelectionSequenceNumberResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetCurrentSelectionSequenceNumberResponse" minOccurs="0"/&gt;
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
    "getCurrentSelectionSequenceNumberResult"
})
@XmlRootElement(name = "GetCurrentSelectionSequenceNumberResponse")
public class GetCurrentSelectionSequenceNumberResponse {

    @XmlElement(name = "GetCurrentSelectionSequenceNumberResult")
    protected GetCurrentSelectionSequenceNumberResponse2 getCurrentSelectionSequenceNumberResult;

    /**
     * Gets the value of the getCurrentSelectionSequenceNumberResult property.
     * 
     * @return
     *     possible object is
     *     {@link GetCurrentSelectionSequenceNumberResponse2 }
     *     
     */
    public GetCurrentSelectionSequenceNumberResponse2 getGetCurrentSelectionSequenceNumberResult() {
        return getCurrentSelectionSequenceNumberResult;
    }

    /**
     * Sets the value of the getCurrentSelectionSequenceNumberResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetCurrentSelectionSequenceNumberResponse2 }
     *     
     */
    public void setGetCurrentSelectionSequenceNumberResult(GetCurrentSelectionSequenceNumberResponse2 value) {
        this.getCurrentSelectionSequenceNumberResult = value;
    }

}
