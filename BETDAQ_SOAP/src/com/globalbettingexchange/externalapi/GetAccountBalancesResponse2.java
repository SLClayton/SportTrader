
package com.globalbettingexchange.externalapi;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * Complex type containing basic account information such as Balance, Exposure and Available funds
 * 
 * <p>Java class for GetAccountBalancesResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetAccountBalancesResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.GlobalBettingExchange.com/ExternalAPI/}BaseResponse"&gt;
 *       &lt;attribute name="Currency" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="Balance" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="Exposure" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="AvailableFunds" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="Credit" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetAccountBalancesResponse")
public class GetAccountBalancesResponse2
    extends BaseResponse
{

    @XmlAttribute(name = "Currency", required = true)
    protected String currency;
    @XmlAttribute(name = "Balance", required = true)
    protected BigDecimal balance;
    @XmlAttribute(name = "Exposure", required = true)
    protected BigDecimal exposure;
    @XmlAttribute(name = "AvailableFunds", required = true)
    protected BigDecimal availableFunds;
    @XmlAttribute(name = "Credit", required = true)
    protected BigDecimal credit;

    /**
     * Gets the value of the currency property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the value of the currency property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrency(String value) {
        this.currency = value;
    }

    /**
     * Gets the value of the balance property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Sets the value of the balance property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setBalance(BigDecimal value) {
        this.balance = value;
    }

    /**
     * Gets the value of the exposure property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getExposure() {
        return exposure;
    }

    /**
     * Sets the value of the exposure property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setExposure(BigDecimal value) {
        this.exposure = value;
    }

    /**
     * Gets the value of the availableFunds property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAvailableFunds() {
        return availableFunds;
    }

    /**
     * Sets the value of the availableFunds property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAvailableFunds(BigDecimal value) {
        this.availableFunds = value;
    }

    /**
     * Gets the value of the credit property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCredit() {
        return credit;
    }

    /**
     * Sets the value of the credit property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCredit(BigDecimal value) {
        this.credit = value;
    }

}
