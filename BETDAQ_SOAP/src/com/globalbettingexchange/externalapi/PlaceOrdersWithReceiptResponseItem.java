
package com.globalbettingexchange.externalapi;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for PlaceOrdersWithReceiptResponseItem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PlaceOrdersWithReceiptResponseItem"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="Status" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="MatchedAgainstStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MatchedStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MatchedPrice" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="UnmatchedStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="Polarity" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="IssuedAt" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="SequenceNumber" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="SelectionId" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="PunterReferenceNumber" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="OrderHandle" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlaceOrdersWithReceiptResponseItem")
public class PlaceOrdersWithReceiptResponseItem {

    @XmlAttribute(name = "Status")
    @XmlSchemaType(name = "unsignedByte")
    protected Short status;
    @XmlAttribute(name = "MatchedAgainstStake")
    protected BigDecimal matchedAgainstStake;
    @XmlAttribute(name = "MatchedStake")
    protected BigDecimal matchedStake;
    @XmlAttribute(name = "MatchedPrice")
    protected BigDecimal matchedPrice;
    @XmlAttribute(name = "UnmatchedStake")
    protected BigDecimal unmatchedStake;
    @XmlAttribute(name = "Polarity")
    @XmlSchemaType(name = "unsignedByte")
    protected Short polarity;
    @XmlAttribute(name = "IssuedAt")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar issuedAt;
    @XmlAttribute(name = "SequenceNumber")
    protected Long sequenceNumber;
    @XmlAttribute(name = "SelectionId")
    protected Long selectionId;
    @XmlAttribute(name = "PunterReferenceNumber")
    protected Long punterReferenceNumber;
    @XmlAttribute(name = "OrderHandle")
    protected Long orderHandle;

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

}
