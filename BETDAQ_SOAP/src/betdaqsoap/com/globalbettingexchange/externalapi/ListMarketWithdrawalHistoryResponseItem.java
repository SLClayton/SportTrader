
package com.globalbettingexchange.externalapi;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for ListMarketWithdrawalHistoryResponseItem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListMarketWithdrawalHistoryResponseItem"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="SelectionId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="WithdrawalTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="SequenceNumber" use="required" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *       &lt;attribute name="ReductionFactor" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="CompoundReductionFactor" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListMarketWithdrawalHistoryResponseItem")
public class ListMarketWithdrawalHistoryResponseItem {

    @XmlAttribute(name = "SelectionId", required = true)
    protected long selectionId;
    @XmlAttribute(name = "WithdrawalTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar withdrawalTime;
    @XmlAttribute(name = "SequenceNumber", required = true)
    protected short sequenceNumber;
    @XmlAttribute(name = "ReductionFactor", required = true)
    protected BigDecimal reductionFactor;
    @XmlAttribute(name = "CompoundReductionFactor", required = true)
    protected BigDecimal compoundReductionFactor;

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
     * Gets the value of the withdrawalTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getWithdrawalTime() {
        return withdrawalTime;
    }

    /**
     * Sets the value of the withdrawalTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setWithdrawalTime(XMLGregorianCalendar value) {
        this.withdrawalTime = value;
    }

    /**
     * Gets the value of the sequenceNumber property.
     * 
     */
    public short getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the value of the sequenceNumber property.
     * 
     */
    public void setSequenceNumber(short value) {
        this.sequenceNumber = value;
    }

    /**
     * Gets the value of the reductionFactor property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getReductionFactor() {
        return reductionFactor;
    }

    /**
     * Sets the value of the reductionFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setReductionFactor(BigDecimal value) {
        this.reductionFactor = value;
    }

    /**
     * Gets the value of the compoundReductionFactor property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCompoundReductionFactor() {
        return compoundReductionFactor;
    }

    /**
     * Sets the value of the compoundReductionFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCompoundReductionFactor(BigDecimal value) {
        this.compoundReductionFactor = value;
    }

}
