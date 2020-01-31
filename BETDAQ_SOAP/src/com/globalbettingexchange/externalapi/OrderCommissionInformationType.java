
package com.globalbettingexchange.externalapi;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OrderCommissionInformationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OrderCommissionInformationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="GrossSettlementAmount" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="OrderCommission" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderCommissionInformationType")
public class OrderCommissionInformationType {

    @XmlAttribute(name = "GrossSettlementAmount", required = true)
    protected BigDecimal grossSettlementAmount;
    @XmlAttribute(name = "OrderCommission")
    protected BigDecimal orderCommission;

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

}
