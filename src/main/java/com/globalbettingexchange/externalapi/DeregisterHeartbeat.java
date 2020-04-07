
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
 *         &lt;element name="deregisterHeartbeatRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}DeregisterHeartbeatRequest" minOccurs="0"/&gt;
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
    "deregisterHeartbeatRequest"
})
@XmlRootElement(name = "DeregisterHeartbeat")
public class DeregisterHeartbeat {

    protected DeregisterHeartbeatRequest deregisterHeartbeatRequest;

    /**
     * Gets the value of the deregisterHeartbeatRequest property.
     * 
     * @return
     *     possible object is
     *     {@link DeregisterHeartbeatRequest }
     *     
     */
    public DeregisterHeartbeatRequest getDeregisterHeartbeatRequest() {
        return deregisterHeartbeatRequest;
    }

    /**
     * Sets the value of the deregisterHeartbeatRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeregisterHeartbeatRequest }
     *     
     */
    public void setDeregisterHeartbeatRequest(DeregisterHeartbeatRequest value) {
        this.deregisterHeartbeatRequest = value;
    }

}
