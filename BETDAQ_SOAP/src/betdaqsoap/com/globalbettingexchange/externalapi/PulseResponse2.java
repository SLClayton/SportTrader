
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for PulseResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PulseResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.GlobalBettingExchange.com/ExternalAPI/}BaseResponse"&gt;
 *       &lt;attribute name="PerformedAt" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="HeartbeatAction" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PulseResponse")
public class PulseResponse2
    extends BaseResponse
{

    @XmlAttribute(name = "PerformedAt")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar performedAt;
    @XmlAttribute(name = "HeartbeatAction")
    @XmlSchemaType(name = "unsignedByte")
    protected Short heartbeatAction;

    /**
     * Gets the value of the performedAt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getPerformedAt() {
        return performedAt;
    }

    /**
     * Sets the value of the performedAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setPerformedAt(XMLGregorianCalendar value) {
        this.performedAt = value;
    }

    /**
     * Gets the value of the heartbeatAction property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getHeartbeatAction() {
        return heartbeatAction;
    }

    /**
     * Sets the value of the heartbeatAction property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setHeartbeatAction(Short value) {
        this.heartbeatAction = value;
    }

}
