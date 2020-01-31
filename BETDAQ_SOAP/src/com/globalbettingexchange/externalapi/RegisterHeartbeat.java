
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
 *         &lt;element name="registerHeartbeatRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}RegisterHeartbeatRequest" minOccurs="0"/&gt;
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
    "registerHeartbeatRequest"
})
@XmlRootElement(name = "RegisterHeartbeat")
public class RegisterHeartbeat {

    protected RegisterHeartbeatRequest registerHeartbeatRequest;

    /**
     * Gets the value of the registerHeartbeatRequest property.
     * 
     * @return
     *     possible object is
     *     {@link RegisterHeartbeatRequest }
     *     
     */
    public RegisterHeartbeatRequest getRegisterHeartbeatRequest() {
        return registerHeartbeatRequest;
    }

    /**
     * Sets the value of the registerHeartbeatRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegisterHeartbeatRequest }
     *     
     */
    public void setRegisterHeartbeatRequest(RegisterHeartbeatRequest value) {
        this.registerHeartbeatRequest = value;
    }

}
