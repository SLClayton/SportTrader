
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;


/**
 * <p>Java class for SettlementInformationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SettlementInformationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="SettledTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="VoidPercentage" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="LeftSideFactor" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="RightSideFactor" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="SettlementResultString" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SettlementInformationType")
public class SettlementInformationType {

    @XmlAttribute(name = "SettledTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar settledTime;
    @XmlAttribute(name = "VoidPercentage", required = true)
    protected BigDecimal voidPercentage;
    @XmlAttribute(name = "LeftSideFactor", required = true)
    protected BigDecimal leftSideFactor;
    @XmlAttribute(name = "RightSideFactor", required = true)
    protected BigDecimal rightSideFactor;
    @XmlAttribute(name = "SettlementResultString", required = true)
    protected String settlementResultString;

    /**
     * Gets the value of the settledTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSettledTime() {
        return settledTime;
    }

    /**
     * Sets the value of the settledTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSettledTime(XMLGregorianCalendar value) {
        this.settledTime = value;
    }

    /**
     * Gets the value of the voidPercentage property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getVoidPercentage() {
        return voidPercentage;
    }

    /**
     * Sets the value of the voidPercentage property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setVoidPercentage(BigDecimal value) {
        this.voidPercentage = value;
    }

    /**
     * Gets the value of the leftSideFactor property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLeftSideFactor() {
        return leftSideFactor;
    }

    /**
     * Sets the value of the leftSideFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLeftSideFactor(BigDecimal value) {
        this.leftSideFactor = value;
    }

    /**
     * Gets the value of the rightSideFactor property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getRightSideFactor() {
        return rightSideFactor;
    }

    /**
     * Sets the value of the rightSideFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setRightSideFactor(BigDecimal value) {
        this.rightSideFactor = value;
    }

    /**
     * Gets the value of the settlementResultString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSettlementResultString() {
        return settlementResultString;
    }

    /**
     * Sets the value of the settlementResultString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSettlementResultString(String value) {
        this.settlementResultString = value;
    }

}
