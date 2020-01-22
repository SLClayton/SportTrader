
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
 *         &lt;element name="ChangeHeartbeatRegistrationResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ChangeHeartbeatRegistrationResponse" minOccurs="0"/&gt;
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
    "changeHeartbeatRegistrationResult"
})
@XmlRootElement(name = "ChangeHeartbeatRegistrationResponse")
public class ChangeHeartbeatRegistrationResponse {

    @XmlElement(name = "ChangeHeartbeatRegistrationResult")
    protected ChangeHeartbeatRegistrationResponse2 changeHeartbeatRegistrationResult;

    /**
     * Gets the value of the changeHeartbeatRegistrationResult property.
     * 
     * @return
     *     possible object is
     *     {@link ChangeHeartbeatRegistrationResponse2 }
     *     
     */
    public ChangeHeartbeatRegistrationResponse2 getChangeHeartbeatRegistrationResult() {
        return changeHeartbeatRegistrationResult;
    }

    /**
     * Sets the value of the changeHeartbeatRegistrationResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChangeHeartbeatRegistrationResponse2 }
     *     
     */
    public void setChangeHeartbeatRegistrationResult(ChangeHeartbeatRegistrationResponse2 value) {
        this.changeHeartbeatRegistrationResult = value;
    }

}
