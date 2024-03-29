
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
 *         &lt;element name="RegisterHeartbeatResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}RegisterHeartbeatResponse" minOccurs="0"/&gt;
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
    "registerHeartbeatResult"
})
@XmlRootElement(name = "RegisterHeartbeatResponse")
public class RegisterHeartbeatResponse {

    @XmlElement(name = "RegisterHeartbeatResult")
    protected RegisterHeartbeatResponse2 registerHeartbeatResult;

    /**
     * Gets the value of the registerHeartbeatResult property.
     * 
     * @return
     *     possible object is
     *     {@link RegisterHeartbeatResponse2 }
     *     
     */
    public RegisterHeartbeatResponse2 getRegisterHeartbeatResult() {
        return registerHeartbeatResult;
    }

    /**
     * Sets the value of the registerHeartbeatResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegisterHeartbeatResponse2 }
     *     
     */
    public void setRegisterHeartbeatResult(RegisterHeartbeatResponse2 value) {
        this.registerHeartbeatResult = value;
    }

}
