
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;


/**
 * <p>Java class for OrderSettlementInformationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OrderSettlementInformationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="GrossSettlementAmount" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="OrderCommission" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MarketCommission" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MarketSettledDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderSettlementInformationType")
public class OrderSettlementInformationType {

    @XmlAttribute(name = "GrossSettlementAmount")
    protected BigDecimal grossSettlementAmount;
    @XmlAttribute(name = "OrderCommission")
    protected BigDecimal orderCommission;
    @XmlAttribute(name = "MarketCommission")
    protected BigDecimal marketCommission;
    @XmlAttribute(name = "MarketSettledDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar marketSettledDate;

    /**
     * Gets the value of the grossSettlementAmount property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getGrossSettlementAmount() {
        return grossSettlementAmount;
    }

    /**
     * Sets the value of the grossSettlementAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setGrossSettlementAmount(BigDecimal value) {
        this.grossSettlementAmount = value;
    }

    /**
     * Gets the value of the orderCommission property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getOrderCommission() {
        return orderCommission;
    }

    /**
     * Sets the value of the orderCommission property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setOrderCommission(BigDecimal value) {
        this.orderCommission = value;
    }

    /**
     * Gets the value of the marketCommission property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMarketCommission() {
        return marketCommission;
    }

    /**
     * Sets the value of the marketCommission property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMarketCommission(BigDecimal value) {
        this.marketCommission = value;
    }

    /**
     * Gets the value of the marketSettledDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getMarketSettledDate() {
        return marketSettledDate;
    }

    /**
     * Sets the value of the marketSettledDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setMarketSettledDate(XMLGregorianCalendar value) {
        this.marketSettledDate = value;
    }

}
