
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RegisterHeartbeatRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegisterHeartbeatRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="ThresholdMs" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="HeartbeatAction" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegisterHeartbeatRequest")
public class RegisterHeartbeatRequest {

    @XmlAttribute(name = "ThresholdMs", required = true)
    protected int thresholdMs;
    @XmlAttribute(name = "HeartbeatAction", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected short heartbeatAction;

    /**
     * Gets the value of the thresholdMs property.
     * 
     */
    public int getThresholdMs() {
        return thresholdMs;
    }

    /**
     * Sets the value of the thresholdMs property.
     * 
     */
    public void setThresholdMs(int value) {
        this.thresholdMs = value;
    }

    /**
     * Gets the value of the heartbeatAction property.
     * 
     */
    public short getHeartbeatAction() {
        return heartbeatAction;
    }

    /**
     * Sets the value of the heartbeatAction property.
     * 
     */
    public void setHeartbeatAction(short value) {
        this.heartbeatAction = value;
    }

}
