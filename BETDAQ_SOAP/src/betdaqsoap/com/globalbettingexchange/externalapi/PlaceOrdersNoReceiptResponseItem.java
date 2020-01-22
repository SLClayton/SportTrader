
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PlaceOrdersNoReceiptResponseItem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PlaceOrdersNoReceiptResponseItem"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="OrderHandle" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="ReturnCode" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="PunterReferenceNumber" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlaceOrdersNoReceiptResponseItem")
public class PlaceOrdersNoReceiptResponseItem {

    @XmlAttribute(name = "OrderHandle")
    protected Long orderHandle;
    @XmlAttribute(name = "ReturnCode", required = true)
    protected int returnCode;
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
     * Gets the value of the returnCode property.
     * 
     */
    public int getReturnCode() {
        return returnCode;
    }

    /**
     * Sets the value of the returnCode property.
     * 
     */
    public void setReturnCode(int value) {
        this.returnCode = value;
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
