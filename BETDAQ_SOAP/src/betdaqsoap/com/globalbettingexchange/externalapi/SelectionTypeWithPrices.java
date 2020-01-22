
package com.globalbettingexchange.externalapi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for SelectionTypeWithPrices complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SelectionTypeWithPrices"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="ForSidePrices" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}PricesType"/&gt;
 *         &lt;element name="AgainstSidePrices" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}PricesType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Id" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="Name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="Status" use="required" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *       &lt;attribute name="ResetCount" use="required" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *       &lt;attribute name="DeductionFactor" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MatchedSelectionForStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="SelectionOpenInterest" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MarketWinnings" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MarketPositiveWinnings" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MatchedSelectionAgainstStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="LastMatchedOccurredAt" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="LastMatchedPrice" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="LastMatchedForSideAmount" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="LastMatchedAgainstSideAmount" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MatchedForSideAmountAtSamePrice" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MatchedAgainstSideAmountAtSamePrice" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="FirstMatchAtSamePriceOccurredAt" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="NumberOrders" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="NumberPunters" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SelectionTypeWithPrices", propOrder = {
    "forSidePricesAndAgainstSidePrices"
})
public class SelectionTypeWithPrices {

    @XmlElementRefs({
        @XmlElementRef(name = "ForSidePrices", namespace = "http://www.GlobalBettingExchange.com/ExternalAPI/", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "AgainstSidePrices", namespace = "http://www.GlobalBettingExchange.com/ExternalAPI/", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<PricesType>> forSidePricesAndAgainstSidePrices;
    @XmlAttribute(name = "Id", required = true)
    protected long id;
    @XmlAttribute(name = "Name", required = true)
    protected String name;
    @XmlAttribute(name = "Status", required = true)
    protected short status;
    @XmlAttribute(name = "ResetCount", required = true)
    protected short resetCount;
    @XmlAttribute(name = "DeductionFactor", required = true)
    protected BigDecimal deductionFactor;
    @XmlAttribute(name = "MatchedSelectionForStake")
    protected BigDecimal matchedSelectionForStake;
    @XmlAttribute(name = "SelectionOpenInterest")
    protected BigDecimal selectionOpenInterest;
    @XmlAttribute(name = "MarketWinnings")
    protected BigDecimal marketWinnings;
    @XmlAttribute(name = "MarketPositiveWinnings")
    protected BigDecimal marketPositiveWinnings;
    @XmlAttribute(name = "MatchedSelectionAgainstStake")
    protected BigDecimal matchedSelectionAgainstStake;
    @XmlAttribute(name = "LastMatchedOccurredAt")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastMatchedOccurredAt;
    @XmlAttribute(name = "LastMatchedPrice")
    protected BigDecimal lastMatchedPrice;
    @XmlAttribute(name = "LastMatchedForSideAmount")
    protected BigDecimal lastMatchedForSideAmount;
    @XmlAttribute(name = "LastMatchedAgainstSideAmount")
    protected BigDecimal lastMatchedAgainstSideAmount;
    @XmlAttribute(name = "MatchedForSideAmountAtSamePrice")
    protected BigDecimal matchedForSideAmountAtSamePrice;
    @XmlAttribute(name = "MatchedAgainstSideAmountAtSamePrice")
    protected BigDecimal matchedAgainstSideAmountAtSamePrice;
    @XmlAttribute(name = "FirstMatchAtSamePriceOccurredAt")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar firstMatchAtSamePriceOccurredAt;
    @XmlAttribute(name = "NumberOrders")
    protected Integer numberOrders;
    @XmlAttribute(name = "NumberPunters")
    protected Integer numberPunters;

    /**
     * Gets the value of the forSidePricesAndAgainstSidePrices property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the forSidePricesAndAgainstSidePrices property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getForSidePricesAndAgainstSidePrices().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link PricesType }{@code >}
     * {@link JAXBElement }{@code <}{@link PricesType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<PricesType>> getForSidePricesAndAgainstSidePrices() {
        if (forSidePricesAndAgainstSidePrices == null) {
            forSidePricesAndAgainstSidePrices = new ArrayList<JAXBElement<PricesType>>();
        }
        return this.forSidePricesAndAgainstSidePrices;
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
     * Gets the value of the deductionFactor property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getDeductionFactor() {
        return deductionFactor;
    }

    /**
     * Sets the value of the deductionFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setDeductionFactor(BigDecimal value) {
        this.deductionFactor = value;
    }

    /**
     * Gets the value of the matchedSelectionForStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMatchedSelectionForStake() {
        return matchedSelectionForStake;
    }

    /**
     * Sets the value of the matchedSelectionForStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMatchedSelectionForStake(BigDecimal value) {
        this.matchedSelectionForStake = value;
    }

    /**
     * Gets the value of the selectionOpenInterest property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSelectionOpenInterest() {
        return selectionOpenInterest;
    }

    /**
     * Sets the value of the selectionOpenInterest property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSelectionOpenInterest(BigDecimal value) {
        this.selectionOpenInterest = value;
    }

    /**
     * Gets the value of the marketWinnings property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMarketWinnings() {
        return marketWinnings;
    }

    /**
     * Sets the value of the marketWinnings property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMarketWinnings(BigDecimal value) {
        this.marketWinnings = value;
    }

    /**
     * Gets the value of the marketPositiveWinnings property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMarketPositiveWinnings() {
        return marketPositiveWinnings;
    }

    /**
     * Sets the value of the marketPositiveWinnings property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMarketPositiveWinnings(BigDecimal value) {
        this.marketPositiveWinnings = value;
    }

    /**
     * Gets the value of the matchedSelectionAgainstStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMatchedSelectionAgainstStake() {
        return matchedSelectionAgainstStake;
    }

    /**
     * Sets the value of the matchedSelectionAgainstStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMatchedSelectionAgainstStake(BigDecimal value) {
        this.matchedSelectionAgainstStake = value;
    }

    /**
     * Gets the value of the lastMatchedOccurredAt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastMatchedOccurredAt() {
        return lastMatchedOccurredAt;
    }

    /**
     * Sets the value of the lastMatchedOccurredAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastMatchedOccurredAt(XMLGregorianCalendar value) {
        this.lastMatchedOccurredAt = value;
    }

    /**
     * Gets the value of the lastMatchedPrice property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLastMatchedPrice() {
        return lastMatchedPrice;
    }

    /**
     * Sets the value of the lastMatchedPrice property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLastMatchedPrice(BigDecimal value) {
        this.lastMatchedPrice = value;
    }

    /**
     * Gets the value of the lastMatchedForSideAmount property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLastMatchedForSideAmount() {
        return lastMatchedForSideAmount;
    }

    /**
     * Sets the value of the lastMatchedForSideAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLastMatchedForSideAmount(BigDecimal value) {
        this.lastMatchedForSideAmount = value;
    }

    /**
     * Gets the value of the lastMatchedAgainstSideAmount property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLastMatchedAgainstSideAmount() {
        return lastMatchedAgainstSideAmount;
    }

    /**
     * Sets the value of the lastMatchedAgainstSideAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLastMatchedAgainstSideAmount(BigDecimal value) {
        this.lastMatchedAgainstSideAmount = value;
    }

    /**
     * Gets the value of the matchedForSideAmountAtSamePrice property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMatchedForSideAmountAtSamePrice() {
        return matchedForSideAmountAtSamePrice;
    }

    /**
     * Sets the value of the matchedForSideAmountAtSamePrice property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMatchedForSideAmountAtSamePrice(BigDecimal value) {
        this.matchedForSideAmountAtSamePrice = value;
    }

    /**
     * Gets the value of the matchedAgainstSideAmountAtSamePrice property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMatchedAgainstSideAmountAtSamePrice() {
        return matchedAgainstSideAmountAtSamePrice;
    }

    /**
     * Sets the value of the matchedAgainstSideAmountAtSamePrice property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMatchedAgainstSideAmountAtSamePrice(BigDecimal value) {
        this.matchedAgainstSideAmountAtSamePrice = value;
    }

    /**
     * Gets the value of the firstMatchAtSamePriceOccurredAt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getFirstMatchAtSamePriceOccurredAt() {
        return firstMatchAtSamePriceOccurredAt;
    }

    /**
     * Sets the value of the firstMatchAtSamePriceOccurredAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFirstMatchAtSamePriceOccurredAt(XMLGregorianCalendar value) {
        this.firstMatchAtSamePriceOccurredAt = value;
    }

    /**
     * Gets the value of the numberOrders property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOrders() {
        return numberOrders;
    }

    /**
     * Sets the value of the numberOrders property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOrders(Integer value) {
        this.numberOrders = value;
    }

    /**
     * Gets the value of the numberPunters property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberPunters() {
        return numberPunters;
    }

    /**
     * Sets the value of the numberPunters property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberPunters(Integer value) {
        this.numberPunters = value;
    }

}
