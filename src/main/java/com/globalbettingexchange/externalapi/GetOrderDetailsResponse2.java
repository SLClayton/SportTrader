
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for GetOrderDetailsResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetOrderDetailsResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.GlobalBettingExchange.com/ExternalAPI/}BaseResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="OrderSettlementInformation" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}OrderSettlementInformationType" minOccurs="0"/&gt;
 *         &lt;element name="AuditLog"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="AuditLog" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}AuditLogItem" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="SelectionId" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="OrderStatus" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="IssuedAt" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="LastChangedAt" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="ExpiresAt" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="ValidFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="RestrictOrderToBroker" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="OrderFillType" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="FillOrKillThreshold" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MarketId" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="MarketStatus" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="RequestedStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="RequestedPrice" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="ExpectedSelectionResetCount" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="TotalStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="UnmatchedStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="AveragePrice" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MatchingTimeStamp" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="Polarity" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="WithdrawalRepriceOption" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="CancelOnInRunning" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="CancelIfSelectionReset" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="SequenceNumber" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="PunterReferenceNumber" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="MarketType" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="ExpectedWithdrawalSequenceNumber" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetOrderDetailsResponse", propOrder = {
    "orderSettlementInformation",
    "auditLog"
})
public class GetOrderDetailsResponse2
    extends BaseResponse
{

    @XmlElement(name = "OrderSettlementInformation")
    protected OrderSettlementInformationType orderSettlementInformation;
    @XmlElement(name = "AuditLog", required = true)
    protected AuditLog auditLog;
    @XmlAttribute(name = "SelectionId")
    protected Long selectionId;
    @XmlAttribute(name = "OrderStatus")
    @XmlSchemaType(name = "unsignedByte")
    protected Short orderStatus;
    @XmlAttribute(name = "IssuedAt")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar issuedAt;
    @XmlAttribute(name = "LastChangedAt")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastChangedAt;
    @XmlAttribute(name = "ExpiresAt")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar expiresAt;
    @XmlAttribute(name = "ValidFrom")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar validFrom;
    @XmlAttribute(name = "RestrictOrderToBroker")
    protected Boolean restrictOrderToBroker;
    @XmlAttribute(name = "OrderFillType")
    @XmlSchemaType(name = "unsignedByte")
    protected Short orderFillType;
    @XmlAttribute(name = "FillOrKillThreshold")
    protected BigDecimal fillOrKillThreshold;
    @XmlAttribute(name = "MarketId")
    protected Long marketId;
    @XmlAttribute(name = "MarketStatus")
    @XmlSchemaType(name = "unsignedByte")
    protected Short marketStatus;
    @XmlAttribute(name = "RequestedStake")
    protected BigDecimal requestedStake;
    @XmlAttribute(name = "RequestedPrice")
    protected BigDecimal requestedPrice;
    @XmlAttribute(name = "ExpectedSelectionResetCount")
    @XmlSchemaType(name = "unsignedByte")
    protected Short expectedSelectionResetCount;
    @XmlAttribute(name = "TotalStake")
    protected BigDecimal totalStake;
    @XmlAttribute(name = "UnmatchedStake")
    protected BigDecimal unmatchedStake;
    @XmlAttribute(name = "AveragePrice")
    protected BigDecimal averagePrice;
    @XmlAttribute(name = "MatchingTimeStamp")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar matchingTimeStamp;
    @XmlAttribute(name = "Polarity")
    @XmlSchemaType(name = "unsignedByte")
    protected Short polarity;
    @XmlAttribute(name = "WithdrawalRepriceOption")
    @XmlSchemaType(name = "unsignedByte")
    protected Short withdrawalRepriceOption;
    @XmlAttribute(name = "CancelOnInRunning")
    protected Boolean cancelOnInRunning;
    @XmlAttribute(name = "CancelIfSelectionReset")
    protected Boolean cancelIfSelectionReset;
    @XmlAttribute(name = "SequenceNumber")
    protected Long sequenceNumber;
    @XmlAttribute(name = "PunterReferenceNumber")
    protected Long punterReferenceNumber;
    @XmlAttribute(name = "MarketType")
    @XmlSchemaType(name = "unsignedByte")
    protected Short marketType;
    @XmlAttribute(name = "ExpectedWithdrawalSequenceNumber")
    @XmlSchemaType(name = "unsignedByte")
    protected Short expectedWithdrawalSequenceNumber;

    /**
     * Gets the value of the orderSettlementInformation property.
     *
     * @return
     *     possible object is
     *     {@link OrderSettlementInformationType }
     *
     */
    public OrderSettlementInformationType getOrderSettlementInformation() {
        return orderSettlementInformation;
    }

    /**
     * Sets the value of the orderSettlementInformation property.
     *
     * @param value
     *     allowed object is
     *     {@link OrderSettlementInformationType }
     *
     */
    public void setOrderSettlementInformation(OrderSettlementInformationType value) {
        this.orderSettlementInformation = value;
    }

    /**
     * Gets the value of the auditLog property.
     *
     * @return
     *     possible object is
     *     {@link GetOrderDetailsResponse2 .AuditLog }
     *
     */
    public AuditLog getAuditLog() {
        return auditLog;
    }

    /**
     * Sets the value of the auditLog property.
     *
     * @param value
     *     allowed object is
     *     {@link GetOrderDetailsResponse2 .AuditLog }
     *
     */
    public void setAuditLog(AuditLog value) {
        this.auditLog = value;
    }

    /**
     * Gets the value of the selectionId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSelectionId() {
        return selectionId;
    }

    /**
     * Sets the value of the selectionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSelectionId(Long value) {
        this.selectionId = value;
    }

    /**
     * Gets the value of the orderStatus property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getOrderStatus() {
        return orderStatus;
    }

    /**
     * Sets the value of the orderStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setOrderStatus(Short value) {
        this.orderStatus = value;
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
     * Gets the value of the lastChangedAt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastChangedAt() {
        return lastChangedAt;
    }

    /**
     * Sets the value of the lastChangedAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastChangedAt(XMLGregorianCalendar value) {
        this.lastChangedAt = value;
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
     * Gets the value of the validFrom property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getValidFrom() {
        return validFrom;
    }

    /**
     * Sets the value of the validFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setValidFrom(XMLGregorianCalendar value) {
        this.validFrom = value;
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

    /**
     * Gets the value of the marketId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getMarketId() {
        return marketId;
    }

    /**
     * Sets the value of the marketId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setMarketId(Long value) {
        this.marketId = value;
    }

    /**
     * Gets the value of the marketStatus property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getMarketStatus() {
        return marketStatus;
    }

    /**
     * Sets the value of the marketStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setMarketStatus(Short value) {
        this.marketStatus = value;
    }

    /**
     * Gets the value of the requestedStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getRequestedStake() {
        return requestedStake;
    }

    /**
     * Sets the value of the requestedStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setRequestedStake(BigDecimal value) {
        this.requestedStake = value;
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
     * Gets the value of the totalStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTotalStake() {
        return totalStake;
    }

    /**
     * Sets the value of the totalStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTotalStake(BigDecimal value) {
        this.totalStake = value;
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
     * Gets the value of the averagePrice property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    /**
     * Sets the value of the averagePrice property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAveragePrice(BigDecimal value) {
        this.averagePrice = value;
    }

    /**
     * Gets the value of the matchingTimeStamp property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getMatchingTimeStamp() {
        return matchingTimeStamp;
    }

    /**
     * Sets the value of the matchingTimeStamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setMatchingTimeStamp(XMLGregorianCalendar value) {
        this.matchingTimeStamp = value;
    }

    /**
     * Gets the value of the polarity property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getPolarity() {
        return polarity;
    }

    /**
     * Sets the value of the polarity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setPolarity(Short value) {
        this.polarity = value;
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
     * Gets the value of the sequenceNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the value of the sequenceNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSequenceNumber(Long value) {
        this.sequenceNumber = value;
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
     * Gets the value of the marketType property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getMarketType() {
        return marketType;
    }

    /**
     * Sets the value of the marketType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setMarketType(Short value) {
        this.marketType = value;
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
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="AuditLog" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}AuditLogItem" maxOccurs="unbounded"/&gt;
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
        "auditLog"
    })
    public static class AuditLog {

        @XmlElement(name = "AuditLog", required = true)
        protected List<AuditLogItem> auditLog;

        /**
         * Gets the value of the auditLog property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the auditLog property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAuditLog().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link AuditLogItem }
         * 
         * 
         */
        public List<AuditLogItem> getAuditLog() {
            if (auditLog == null) {
                auditLog = new ArrayList<AuditLogItem>();
            }
            return this.auditLog;
        }

    }

}
