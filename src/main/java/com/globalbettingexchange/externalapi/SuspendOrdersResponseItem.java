
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;


/**
 * <p>Java class for SuspendOrdersResponseItem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SuspendOrdersResponseItem"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="OrderId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="SuspendedForSideStake" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="PunterReferenceNumber" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SuspendOrdersResponseItem")
public class SuspendOrdersResponseItem {

    @XmlAttribute(name = "OrderId", required = true)
    protected long orderId;
    @XmlAttribute(name = "SuspendedForSideStake", required = true)
    protected BigDecimal suspendedForSideStake;
    @XmlAttribute(name = "PunterReferenceNumber", required = true)
    protected long punterReferenceNumber;

    /**
     * Gets the value of the orderId property.
     * 
     */
    public long getOrderId() {
        return orderId;
    }

    /**
     * Sets the value of the orderId property.
     * 
     */
    public void setOrderId(long value) {
        this.orderId = value;
    }

    /**
     * Gets the value of the suspendedForSideStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSuspendedForSideStake() {
        return suspendedForSideStake;
    }

    /**
     * Sets the value of the suspendedForSideStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSuspendedForSideStake(BigDecimal value) {
        this.suspendedForSideStake = value;
    }

    /**
     * Gets the value of the punterReferenceNumber property.
     * 
     */
    public long getPunterReferenceNumber() {
        return punterReferenceNumber;
    }

    /**
     * Sets the value of the punterReferenceNumber property.
     * 
     */
    public void setPunterReferenceNumber(long value) {
        this.punterReferenceNumber = value;
    }

}
