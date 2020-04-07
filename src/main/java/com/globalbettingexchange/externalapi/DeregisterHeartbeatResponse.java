
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
 *         &lt;element name="DeregisterHeartbeatResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}DeregisterHeartbeatResponse" minOccurs="0"/&gt;
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
    "deregisterHeartbeatResult"
})
@XmlRootElement(name = "DeregisterHeartbeatResponse")
public class DeregisterHeartbeatResponse {

    @XmlElement(name = "DeregisterHeartbeatResult")
    protected DeregisterHeartbeatResponse2 deregisterHeartbeatResult;

    /**
     * Gets the value of the deregisterHeartbeatResult property.
     * 
     * @return
     *     possible object is
     *     {@link DeregisterHeartbeatResponse2 }
     *     
     */
    public DeregisterHeartbeatResponse2 getDeregisterHeartbeatResult() {
        return deregisterHeartbeatResult;
    }

    /**
     * Sets the value of the deregisterHeartbeatResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeregisterHeartbeatResponse2 }
     *     
     */
    public void setDeregisterHeartbeatResult(DeregisterHeartbeatResponse2 value) {
        this.deregisterHeartbeatResult = value;
    }

}
