
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;


/**
 * <p>Java class for MatchedOrderInformationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MatchedOrderInformationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="MatchedStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MatchedAgainstStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="PriceMatched" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MatchedOrderID" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="WasMake" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MatchedOrderInformationType")
public class MatchedOrderInformationType {

    @XmlAttribute(name = "MatchedStake")
    protected BigDecimal matchedStake;
    @XmlAttribute(name = "MatchedAgainstStake")
    protected BigDecimal matchedAgainstStake;
    @XmlAttribute(name = "PriceMatched")
    protected BigDecimal priceMatched;
    @XmlAttribute(name = "MatchedOrderID")
    protected Long matchedOrderID;
    @XmlAttribute(name = "WasMake")
    protected Boolean wasMake;

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
     * Gets the value of the priceMatched property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getPriceMatched() {
        return priceMatched;
    }

    /**
     * Sets the value of the priceMatched property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setPriceMatched(BigDecimal value) {
        this.priceMatched = value;
    }

    /**
     * Gets the value of the matchedOrderID property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getMatchedOrderID() {
        return matchedOrderID;
    }

    /**
     * Sets the value of the matchedOrderID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setMatchedOrderID(Long value) {
        this.matchedOrderID = value;
    }

    /**
     * Gets the value of the wasMake property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWasMake() {
        return wasMake;
    }

    /**
     * Sets the value of the wasMake property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWasMake(Boolean value) {
        this.wasMake = value;
    }

}
