
package com.globalbettingexchange.externalapi;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for AuditLogItem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AuditLogItem"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="MatchedOrderInformation" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}MatchedOrderInformationType"/&gt;
 *         &lt;element name="CommissionInformation" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}CommissionInformationType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Time" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="OrderActionType" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="RequestedStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="TotalStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="TotalAgainstStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="RequestedPrice" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="AveragePrice" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuditLogItem", propOrder = {
    "matchedOrderInformation",
    "commissionInformation"
})
public class AuditLogItem {

    @XmlElement(name = "MatchedOrderInformation", required = true)
    protected MatchedOrderInformationType matchedOrderInformation;
    @XmlElement(name = "CommissionInformation")
    protected CommissionInformationType commissionInformation;
    @XmlAttribute(name = "Time")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar time;
    @XmlAttribute(name = "OrderActionType")
    @XmlSchemaType(name = "unsignedByte")
    protected Short orderActionType;
    @XmlAttribute(name = "RequestedStake")
    protected BigDecimal requestedStake;
    @XmlAttribute(name = "TotalStake")
    protected BigDecimal totalStake;
    @XmlAttribute(name = "TotalAgainstStake")
    protected BigDecimal totalAgainstStake;
    @XmlAttribute(name = "RequestedPrice")
    protected BigDecimal requestedPrice;
    @XmlAttribute(name = "AveragePrice")
    protected BigDecimal averagePrice;

    /**
     * Gets the value of the matchedOrderInformation property.
     * 
     * @return
     *     possible object is
     *     {@link MatchedOrderInformationType }
     *     
     */
    public MatchedOrderInformationType getMatchedOrderInformation() {
        return matchedOrderInformation;
    }

    /**
     * Sets the value of the matchedOrderInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link MatchedOrderInformationType }
     *     
     */
    public void setMatchedOrderInformation(MatchedOrderInformationType value) {
        this.matchedOrderInformation = value;
    }

    /**
     * Gets the value of the commissionInformation property.
     * 
     * @return
     *     possible object is
     *     {@link CommissionInformationType }
     *     
     */
    public CommissionInformationType getCommissionInformation() {
        return commissionInformation;
    }

    /**
     * Sets the value of the commissionInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommissionInformationType }
     *     
     */
    public void setCommissionInformation(CommissionInformationType value) {
        this.commissionInformation = value;
    }

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTime(XMLGregorianCalendar value) {
        this.time = value;
    }

    /**
     * Gets the value of the orderActionType property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getOrderActionType() {
        return orderActionType;
    }

    /**
     * Sets the value of the orderActionType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setOrderActionType(Short value) {
        this.orderActionType = value;
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
     * Gets the value of the totalAgainstStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTotalAgainstStake() {
        return totalAgainstStake;
    }

    /**
     * Sets the value of the totalAgainstStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTotalAgainstStake(BigDecimal value) {
        this.totalAgainstStake = value;
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

}
