
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;


/**
 * <p>Java class for CancelAllOrdersResponseItem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CancelAllOrdersResponseItem"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="OrderHandle" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="cancelledForSideStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="PunterReferenceNumber" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CancelAllOrdersResponseItem")
public class CancelAllOrdersResponseItem {

    @XmlAttribute(name = "OrderHandle")
    protected Long orderHandle;
    @XmlAttribute(name = "cancelledForSideStake")
    protected BigDecimal cancelledForSideStake;
    @XmlAttribute(name = "PunterReferenceNumber", required = true)
    protected long punterReferenceNumber;

    /**
     * Gets the value of the orderHandle property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getOrderHandle() {
        return orderHandle;
    }

    /**
     * Sets the value of the orderHandle property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setOrderHandle(Long value) {
        this.orderHandle = value;
    }

    /**
     * Gets the value of the cancelledForSideStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCancelledForSideStake() {
        return cancelledForSideStake;
    }

    /**
     * Sets the value of the cancelledForSideStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCancelledForSideStake(BigDecimal value) {
        this.cancelledForSideStake = value;
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
