
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
 *         &lt;element name="PulseResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}PulseResponse" minOccurs="0"/&gt;
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
    "pulseResult"
})
@XmlRootElement(name = "PulseResponse")
public class PulseResponse {

    @XmlElement(name = "PulseResult")
    protected PulseResponse2 pulseResult;

    /**
     * Gets the value of the pulseResult property.
     * 
     * @return
     *     possible object is
     *     {@link PulseResponse2 }
     *     
     */
    public PulseResponse2 getPulseResult() {
        return pulseResult;
    }

    /**
     * Sets the value of the pulseResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link PulseResponse2 }
     *     
     */
    public void setPulseResult(PulseResponse2 value) {
        this.pulseResult = value;
    }

}
