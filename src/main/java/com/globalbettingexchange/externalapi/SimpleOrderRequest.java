
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;


/**
 * A type representing an Order Request with no receipt
 * 
 * <p>Java class for SimpleOrderRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SimpleOrderRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="SelectionId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="Stake" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="Price" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="Polarity" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="ExpectedSelectionResetCount" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="ExpectedWithdrawalSequenceNumber" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="CancelOnInRunning" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="CancelIfSelectionReset" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="ExpiresAt" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="WithdrawalRepriceOption" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="KillType" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="FillOrKillThreshold" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="RestrictOrderToBroker" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="ChannelTypeInfo" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="PunterReferenceNumber" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimpleOrderRequest")
public class SimpleOrderRequest {

    @XmlAttribute(name = "SelectionId", required = true)
    protected long selectionId;
    @XmlAttribute(name = "Stake", required = true)
    protected BigDecimal stake;
    @XmlAttribute(name = "Price", required = true)
    protected BigDecimal price;
    @XmlAttribute(name = "Polarity", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected short polarity;
    @XmlAttribute(name = "ExpectedSelectionResetCount", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected short expectedSelectionResetCount;
    @XmlAttribute(name = "ExpectedWithdrawalSequenceNumber", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected short expectedWithdrawalSequenceNumber;
    @XmlAttribute(name = "CancelOnInRunning", required = true)
    protected boolean cancelOnInRunning;
    @XmlAttribute(name = "CancelIfSelectionReset", required = true)
    protected boolean cancelIfSelectionReset;
    @XmlAttribute(name = "ExpiresAt")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar expiresAt;
    @XmlAttribute(name = "WithdrawalRepriceOption")
    @XmlSchemaType(name = "unsignedByte")
    protected Short withdrawalRepriceOption;
    @XmlAttribute(name = "KillType")
    @XmlSchemaType(name = "unsignedByte")
    protected Short killType;
    @XmlAttribute(name = "FillOrKillThreshold")
    protected BigDecimal fillOrKillThreshold;
    @XmlAttribute(name = "RestrictOrderToBroker")
    protected Boolean restrictOrderToBroker;
    @XmlAttribute(name = "ChannelTypeInfo")
    protected String channelTypeInfo;
    @XmlAttribute(name = "PunterReferenceNumber")
    protected Long punterReferenceNumber;

    /**
     * Gets the value of the selectionId property.
     * 
     */
    public long getSelectionId() {
        return selectionId;
    }

    /**
     * Sets the value of the selectionId property.
     * 
     */
    public void setSelectionId(long value) {
        this.selectionId = value;
    }

    /**
     * Gets the value of the stake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getStake() {
        return stake;
    }

    /**
     * Sets the value of the stake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setStake(BigDecimal value) {
        this.stake = value;
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
     * Gets the value of the polarity property.
     * 
     */
    public short getPolarity() {
        return polarity;
    }

    /**
     * Sets the value of the polarity property.
     * 
     */
    public void setPolarity(short value) {
        this.polarity = value;
    }

    /**
     * Gets the value of the expectedSelectionResetCount property.
     * 
     */
    public short getExpectedSelectionResetCount() {
        return expectedSelectionResetCount;
    }

    /**
     * Sets the value of the expectedSelectionResetCount property.
     * 
     */
    public void setExpectedSelectionResetCount(short value) {
        this.expectedSelectionResetCount = value;
    }

    /**
     * Gets the value of the expectedWithdrawalSequenceNumber property.
     * 
     */
    public short getExpectedWithdrawalSequenceNumber() {
        return expectedWithdrawalSequenceNumber;
    }

    /**
     * Sets the value of the expectedWithdrawalSequenceNumber property.
     * 
     */
    public void setExpectedWithdrawalSequenceNumber(short value) {
        this.expectedWithdrawalSequenceNumber = value;
    }

    /**
     * Gets the value of the cancelOnInRunning property.
     * 
     */
    public boolean isCancelOnInRunning() {
        return cancelOnInRunning;
    }

    /**
     * Sets the value of the cancelOnInRunning property.
     * 
     */
    public void setCancelOnInRunning(boolean value) {
        this.cancelOnInRunning = value;
    }

    /**
     * Gets the value of the cancelIfSelectionReset property.
     * 
     */
    public boolean isCancelIfSelectionReset() {
        return cancelIfSelectionReset;
    }

    /**
     * Sets the value of the cancelIfSelectionReset property.
     * 
     */
    public void setCancelIfSelectionReset(boolean value) {
        this.cancelIfSelectionReset = value;
    }

    /**
     * Gets the value of the expiresAt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets the value of the expiresAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setExpiresAt(XMLGregorianCalendar value) {
        this.expiresAt = value;
    }

    /**
     * Gets the value of the withdrawalRepriceOption property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getWithdrawalRepriceOption() {
        return withdrawalRepriceOption;
    }

    /**
     * Sets the value of the withdrawalRepriceOption property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setWithdrawalRepriceOption(Short value) {
        this.withdrawalRepriceOption = value;
    }

    /**
     * Gets the value of the killType property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getKillType() {
        return killType;
    }

    /**
     * Sets the value of the killType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setKillType(Short value) {
        this.killType = value;
    }

    /**
     * Gets the value of the fillOrKillThreshold property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getFillOrKillThreshold() {
        return fillOrKillThreshold;
    }

    /**
     * Sets the value of the fillOrKillThreshold property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setFillOrKillThreshold(BigDecimal value) {
        this.fillOrKillThreshold = value;
    }

    /**
     * Gets the value of the restrictOrderToBroker property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRestrictOrderToBroker() {
        return restrictOrderToBroker;
    }

    /**
     * Sets the value of the restrictOrderToBroker property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRestrictOrderToBroker(Boolean value) {
        this.restrictOrderToBroker = value;
    }

    /**
     * Gets the value of the channelTypeInfo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChannelTypeInfo() {
        return channelTypeInfo;
    }

    /**
     * Sets the value of the channelTypeInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChannelTypeInfo(String value) {
        this.channelTypeInfo = value;
    }

    /**
     * Gets the value of the punterReferenceNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getPunterReferenceNumber() {
        return punterReferenceNumber;
    }

    /**
     * Sets the value of the punterReferenceNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPunterReferenceNumber(Long value) {
        this.punterReferenceNumber = value;
    }

}
