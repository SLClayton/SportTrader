
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;


/**
 * <p>Java class for UpdateOrdersNoReceiptRequestItem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UpdateOrdersNoReceiptRequestItem"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="BetId" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="DeltaStake" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="Price" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="ExpectedSelectionResetCount" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="ExpectedWithdrawalSequenceNumber" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="CancelOnInRunning" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="CancelIfSelectionReset" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="SetToBeSPIfUnmatched" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateOrdersNoReceiptRequestItem")
public class UpdateOrdersNoReceiptRequestItem {

    @XmlAttribute(name = "BetId")
    protected Long betId;
    @XmlAttribute(name = "DeltaStake", required = true)
    protected BigDecimal deltaStake;
    @XmlAttribute(name = "Price", required = true)
    protected BigDecimal price;
    @XmlAttribute(name = "ExpectedSelectionResetCount")
    @XmlSchemaType(name = "unsignedByte")
    protected Short expectedSelectionResetCount;
    @XmlAttribute(name = "ExpectedWithdrawalSequenceNumber")
    @XmlSchemaType(name = "unsignedByte")
    protected Short expectedWithdrawalSequenceNumber;
    @XmlAttribute(name = "CancelOnInRunning")
    protected Boolean cancelOnInRunning;
    @XmlAttribute(name = "CancelIfSelectionReset")
    protected Boolean cancelIfSelectionReset;
    @XmlAttribute(name = "SetToBeSPIfUnmatched")
    protected Boolean setToBeSPIfUnmatched;

    /**
     * Gets the value of the betId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getBetId() {
        return betId;
    }

    /**
     * Sets the value of the betId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setBetId(Long value) {
        this.betId = value;
    }

    /**
     * Gets the value of the deltaStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getDeltaStake() {
        return deltaStake;
    }

    /**
     * Sets the value of the deltaStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setDeltaStake(BigDecimal value) {
        this.deltaStake = value;
    }

    /**
     * Gets the value of the price property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets the value of the price property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setPrice(BigDecimal value) {
        this.price = value;
    }

    /**
     * Gets the value of the expectedSelectionResetCount property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getExpectedSelectionResetCount() {
        return expectedSelectionResetCount;
    }

    /**
     * Sets the value of the expectedSelectionResetCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setExpectedSelectionResetCount(Short value) {
        this.expectedSelectionResetCount = value;
    }

    /**
     * Gets the value of the expectedWithdrawalSequenceNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getExpectedWithdrawalSequenceNumber() {
        return expectedWithdrawalSequenceNumber;
    }

    /**
     * Sets the value of the expectedWithdrawalSequenceNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setExpectedWithdrawalSequenceNumber(Short value) {
        this.expectedWithdrawalSequenceNumber = value;
    }

    /**
     * Gets the value of the cancelOnInRunning property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCancelOnInRunning() {
        return cancelOnInRunning;
    }

    /**
     * Sets the value of the cancelOnInRunning property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCancelOnInRunning(Boolean value) {
        this.cancelOnInRunning = value;
    }

    /**
     * Gets the value of the cancelIfSelectionReset property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCancelIfSelectionReset() {
        return cancelIfSelectionReset;
    }

    /**
     * Sets the value of the cancelIfSelectionReset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCancelIfSelectionReset(Boolean value) {
        this.cancelIfSelectionReset = value;
    }

    /**
     * Gets the value of the setToBeSPIfUnmatched property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSetToBeSPIfUnmatched() {
        return setToBeSPIfUnmatched;
    }

    /**
     * Sets the value of the setToBeSPIfUnmatched property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSetToBeSPIfUnmatched(Boolean value) {
        this.setToBeSPIfUnmatched = value;
    }

}
