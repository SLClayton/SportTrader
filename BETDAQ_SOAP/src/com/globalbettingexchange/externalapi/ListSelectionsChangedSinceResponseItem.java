
package com.globalbettingexchange.externalapi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for ListSelectionsChangedSinceResponseItem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListSelectionsChangedSinceResponseItem"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="SettlementInformation" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}SettlementInformationType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Id" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="Name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="DisplayOrder" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="IsHidden" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="Status" use="required" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *       &lt;attribute name="ResetCount" use="required" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *       &lt;attribute name="WithdrawalFactor" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MarketId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="SelectionSequenceNumber" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="CancelOrdersTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListSelectionsChangedSinceResponseItem", propOrder = {
    "settlementInformation"
})
public class ListSelectionsChangedSinceResponseItem {

    @XmlElement(name = "SettlementInformation")
    protected List<SettlementInformationType> settlementInformation;
    @XmlAttribute(name = "Id", required = true)
    protected long id;
    @XmlAttribute(name = "Name", required = true)
    protected String name;
    @XmlAttribute(name = "DisplayOrder", required = true)
    protected int displayOrder;
    @XmlAttribute(name = "IsHidden", required = true)
    protected boolean isHidden;
    @XmlAttribute(name = "Status", required = true)
    protected short status;
    @XmlAttribute(name = "ResetCount", required = true)
    protected short resetCount;
    @XmlAttribute(name = "WithdrawalFactor", required = true)
    protected BigDecimal withdrawalFactor;
    @XmlAttribute(name = "MarketId", required = true)
    protected long marketId;
    @XmlAttribute(name = "SelectionSequenceNumber", required = true)
    protected long selectionSequenceNumber;
    @XmlAttribute(name = "CancelOrdersTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar cancelOrdersTime;

    /**
     * Gets the value of the settlementInformation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the settlementInformation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSettlementInformation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SettlementInformationType }
     * 
     * 
     */
    public List<SettlementInformationType> getSettlementInformation() {
        if (settlementInformation == null) {
            settlementInformation = new ArrayList<SettlementInformationType>();
        }
        return this.settlementInformation;
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
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the displayOrder property.
     * 
     */
    public int getDisplayOrder() {
        return displayOrder;
    }

    /**
     * Sets the value of the displayOrder property.
     * 
     */
    public void setDisplayOrder(int value) {
        this.displayOrder = value;
    }

    /**
     * Gets the value of the isHidden property.
     * 
     */
    public boolean isIsHidden() {
        return isHidden;
    }

    /**
     * Sets the value of the isHidden property.
     * 
     */
    public void setIsHidden(boolean value) {
        this.isHidden = value;
    }

    /**
     * Gets the value of the status property.
     * 
     */
    public short getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     */
    public void setStatus(short value) {
        this.status = value;
    }

    /**
     * Gets the value of the resetCount property.
     * 
     */
    public short getResetCount() {
        return resetCount;
    }

    /**
     * Sets the value of the resetCount property.
     * 
     */
    public void setResetCount(short value) {
        this.resetCount = value;
    }

    /**
     * Gets the value of the withdrawalFactor property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getWithdrawalFactor() {
        return withdrawalFactor;
    }

    /**
     * Sets the value of the withdrawalFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setWithdrawalFactor(BigDecimal value) {
        this.withdrawalFactor = value;
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
     * Gets the value of the selectionSequenceNumber property.
     * 
     */
    public long getSelectionSequenceNumber() {
        return selectionSequenceNumber;
    }

    /**
     * Sets the value of the selectionSequenceNumber property.
     * 
     */
    public void setSelectionSequenceNumber(long value) {
        this.selectionSequenceNumber = value;
    }

    /**
     * Gets the value of the cancelOrdersTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCancelOrdersTime() {
        return cancelOrdersTime;
    }

    /**
     * Sets the value of the cancelOrdersTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCancelOrdersTime(XMLGregorianCalendar value) {
        this.cancelOrdersTime = value;
    }

}
