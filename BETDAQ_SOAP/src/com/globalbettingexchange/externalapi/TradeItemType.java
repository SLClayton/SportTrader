
package com.globalbettingexchange.externalapi;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for TradeItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TradeItemType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="occurredAt" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="price" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="backersStake" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="layersLiability" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="tradeType" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TradeItemType")
public class TradeItemType {

    @XmlAttribute(name = "occurredAt", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar occurredAt;
    @XmlAttribute(name = "price", required = true)
    protected BigDecimal price;
    @XmlAttribute(name = "backersStake", required = true)
    protected BigDecimal backersStake;
    @XmlAttribute(name = "layersLiability", required = true)
    protected BigDecimal layersLiability;
    @XmlAttribute(name = "tradeType", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected short tradeType;

    /**
     * Gets the value of the occurredAt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getOccurredAt() {
        return occurredAt;
    }

    /**
     * Sets the value of the occurredAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setOccurredAt(XMLGregorianCalendar value) {
        this.occurredAt = value;
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
     * Gets the value of the backersStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getBackersStake() {
        return backersStake;
    }

    /**
     * Sets the value of the backersStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setBackersStake(BigDecimal value) {
        this.backersStake = value;
    }

    /**
     * Gets the value of the layersLiability property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLayersLiability() {
        return layersLiability;
    }

    /**
     * Sets the value of the layersLiability property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLayersLiability(BigDecimal value) {
        this.layersLiability = value;
    }

    /**
     * Gets the value of the tradeType property.
     * 
     */
    public short getTradeType() {
        return tradeType;
    }

    /**
     * Sets the value of the tradeType property.
     * 
     */
    public void setTradeType(short value) {
        this.tradeType = value;
    }

}
