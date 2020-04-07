
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;


/**
 * A type representing an Order
 * 
 * <p>Java class for Order complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Order"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="OrderCommissionInformation" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}OrderCommissionInformationType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Id" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="MarketId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="SelectionId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="SequenceNumber" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="IssuedAt" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="Polarity" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="UnmatchedStake" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="RequestedPrice" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MatchedPrice" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MatchedStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="TotalForSideMakeStake" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="TotalForSideTakeStake" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MatchedAgainstStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="Status" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="RestrictOrderToBroker" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="PunterReferenceNumber" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="CancelOnInRunning" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="CancelIfSelectionReset" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="IsCurrentlyInRunning" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="PunterCommissionBasis" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="MakeCommissionRate" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="TakeCommissionRate" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="ExpectedSelectionResetCount" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="ExpectedWithdrawalSequenceNumber" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="OrderFillType" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="FillOrKillThreshold" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Order", propOrder = {
    "orderCommissionInformation"
})
public class Order {

    @XmlElement(name = "OrderCommissionInformation")
    protected OrderCommissionInformationType orderCommissionInformation;
    @XmlAttribute(name = "Id", required = true)
    protected long id;
    @XmlAttribute(name = "MarketId", required = true)
    protected long marketId;
    @XmlAttribute(name = "SelectionId", required = true)
    protected long selectionId;
    @XmlAttribute(name = "SequenceNumber", required = true)
    protected long sequenceNumber;
    @XmlAttribute(name = "IssuedAt", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar issuedAt;
    @XmlAttribute(name = "Polarity", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected short polarity;
    @XmlAttribute(name = "UnmatchedStake", required = true)
    protected BigDecimal unmatchedStake;
    @XmlAttribute(name = "RequestedPrice", required = true)
    protected BigDecimal requestedPrice;
    @XmlAttribute(name = "MatchedPrice")
    protected BigDecimal matchedPrice;
    @XmlAttribute(name = "MatchedStake")
    protected BigDecimal matchedStake;
    @XmlAttribute(name = "TotalForSideMakeStake", required = true)
    protected BigDecimal totalForSideMakeStake;
    @XmlAttribute(name = "TotalForSideTakeStake", required = true)
    protected BigDecimal totalForSideTakeStake;
    @XmlAttribute(name = "MatchedAgainstStake")
    protected BigDecimal matchedAgainstStake;
    @XmlAttribute(name = "Status")
    @XmlSchemaType(name = "unsignedByte")
    protected Short status;
    @XmlAttribute(name = "RestrictOrderToBroker")
    protected Boolean restrictOrderToBroker;
    @XmlAttribute(name = "PunterReferenceNumber")
    protected Long punterReferenceNumber;
    @XmlAttribute(name = "CancelOnInRunning", required = true)
    protected boolean cancelOnInRunning;
    @XmlAttribute(name = "CancelIfSelectionReset", required = true)
    protected boolean cancelIfSelectionReset;
    @XmlAttribute(name = "IsCurrentlyInRunning", required = true)
    protected boolean isCurrentlyInRunning;
    @XmlAttribute(name = "PunterCommissionBasis", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected short punterCommissionBasis;
    @XmlAttribute(name = "MakeCommissionRate", required = true)
    protected BigDecimal makeCommissionRate;
    @XmlAttribute(name = "TakeCommissionRate", required = true)
    protected BigDecimal takeCommissionRate;
    @XmlAttribute(name = "ExpectedSelectionResetCount", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected short expectedSelectionResetCount;
    @XmlAttribute(name = "ExpectedWithdrawalSequenceNumber", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected short expectedWithdrawalSequenceNumber;
    @XmlAttribute(name = "OrderFillType")
    @XmlSchemaType(name = "unsignedByte")
    protected Short orderFillType;
    @XmlAttribute(name = "FillOrKillThreshold")
    protected BigDecimal fillOrKillThreshold;

    /**
     * Gets the value of the orderCommissionInformation property.
     * 
     * @return
     *     possible object is
     *     {@link OrderCommissionInformationType }
     *     
     */
    public OrderCommissionInformationType getOrderCommissionInformation() {
        return orderCommissionInformation;
    }

    /**
     * Sets the value of the orderCommissionInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderCommissionInformationType }
     *     
     */
    public void setOrderCommissionInformation(OrderCommissionInformationType value) {
        this.orderCommissionInformation = value;
    }

    /**
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Gets the value of the marketId property.
     * 
     */
    public long getMarketId() {
        return marketId;
    }

    /**
     * Sets the value of the marketId property.
     * 
     */
    public void setMarketId(long value) {
        this.marketId = value;
    }

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
     * Gets the value of the sequenceNumber property.
     * 
     */
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the value of the sequenceNumber property.
     * 
     */
    public void setSequenceNumber(long value) {
        this.sequenceNumber = value;
    }

    /**
     * Gets the value of the issuedAt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getIssuedAt() {
        return issuedAt;
    }

    /**
     * Sets the value of the issuedAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setIssuedAt(XMLGregorianCalendar value) {
        this.issuedAt = value;
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
     * Gets the value of the unmatchedStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getUnmatchedStake() {
        return unmatchedStake;
    }

    /**
     * Sets the value of the unmatchedStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setUnmatchedStake(BigDecimal value) {
        this.unmatchedStake = value;
    }

    /**
     * Gets the value of the requestedPrice property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getRequestedPrice() {
        return requestedPrice;
    }

    /**
     * Sets the value of the requestedPrice property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setRequestedPrice(BigDecimal value) {
        this.requestedPrice = value;
    }

    /**
     * Gets the value of the matchedPrice property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMatchedPrice() {
        return matchedPrice;
    }

    /**
     * Sets the value of the matchedPrice property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMatchedPrice(BigDecimal value) {
        this.matchedPrice = value;
    }

    /**
     * Gets the value of the matchedStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMatchedStake() {
        return matchedStake;
    }

    /**
     * Sets the value of the matchedStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMatchedStake(BigDecimal value) {
        this.matchedStake = value;
    }

    /**
     * Gets the value of the totalForSideMakeStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTotalForSideMakeStake() {
        return totalForSideMakeStake;
    }

    /**
     * Sets the value of the totalForSideMakeStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTotalForSideMakeStake(BigDecimal value) {
        this.totalForSideMakeStake = value;
    }

    /**
     * Gets the value of the totalForSideTakeStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTotalForSideTakeStake() {
        return totalForSideTakeStake;
    }

    /**
     * Sets the value of the totalForSideTakeStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTotalForSideTakeStake(BigDecimal value) {
        this.totalForSideTakeStake = value;
    }

    /**
     * Gets the value of the matchedAgainstStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMatchedAgainstStake() {
        return matchedAgainstStake;
    }

    /**
     * Sets the value of the matchedAgainstStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMatchedAgainstStake(BigDecimal value) {
        this.matchedAgainstStake = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setStatus(Short value) {
        this.status = value;
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
     * Gets the value of the isCurrentlyInRunning property.
     * 
     */
    public boolean isIsCurrentlyInRunning() {
        return isCurrentlyInRunning;
    }

    /**
     * Sets the value of the isCurrentlyInRunning property.
     * 
     */
    public void setIsCurrentlyInRunning(boolean value) {
        this.isCurrentlyInRunning = value;
    }

    /**
     * Gets the value of the punterCommissionBasis property.
     * 
     */
    public short getPunterCommissionBasis() {
        return punterCommissionBasis;
    }

    /**
     * Sets the value of the punterCommissionBasis property.
     * 
     */
    public void setPunterCommissionBasis(short value) {
        this.punterCommissionBasis = value;
    }

    /**
     * Gets the value of the makeCommissionRate property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMakeCommissionRate() {
        return makeCommissionRate;
    }

    /**
     * Sets the value of the makeCommissionRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMakeCommissionRate(BigDecimal value) {
        this.makeCommissionRate = value;
    }

    /**
     * Gets the value of the takeCommissionRate property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTakeCommissionRate() {
        return takeCommissionRate;
    }

    /**
     * Sets the value of the takeCommissionRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTakeCommissionRate(BigDecimal value) {
        this.takeCommissionRate = value;
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
     * Gets the value of the orderFillType property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getOrderFillType() {
        return orderFillType;
    }

    /**
     * Sets the value of the orderFillType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setOrderFillType(Short value) {
        this.orderFillType = value;
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

}
