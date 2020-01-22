
package com.globalbettingexchange.externalapi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for MarketTypeWithPrices complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MarketTypeWithPrices"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Selections" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}SelectionTypeWithPrices" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Id" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="Name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="Type" use="required" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *       &lt;attribute name="IsPlayMarket" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="Status" use="required" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *       &lt;attribute name="NumberOfWinningSelections" use="required" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *       &lt;attribute name="StartTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="WithdrawalSequenceNumber" use="required" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *       &lt;attribute name="DisplayOrder" use="required" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *       &lt;attribute name="IsEnabledForMultiples" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="IsInRunningAllowed" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="IsManagedWhenInRunning" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="IsCurrentlyInRunning" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="InRunningDelaySeconds" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="ReturnCode" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="TotalMatchedAmount" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="PlacePayout" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MatchedMarketForStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="MatchedMarketAgainstStake" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="HomeTeamScore" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="AwayTeamScore" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="ScoreType" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MarketTypeWithPrices", propOrder = {
    "selections"
})
public class MarketTypeWithPrices {

    @XmlElement(name = "Selections")
    protected List<SelectionTypeWithPrices> selections;
    @XmlAttribute(name = "Id", required = true)
    protected long id;
    @XmlAttribute(name = "Name", required = true)
    protected String name;
    @XmlAttribute(name = "Type", required = true)
    protected short type;
    @XmlAttribute(name = "IsPlayMarket", required = true)
    protected boolean isPlayMarket;
    @XmlAttribute(name = "Status", required = true)
    protected short status;
    @XmlAttribute(name = "NumberOfWinningSelections", required = true)
    protected short numberOfWinningSelections;
    @XmlAttribute(name = "StartTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startTime;
    @XmlAttribute(name = "WithdrawalSequenceNumber", required = true)
    protected short withdrawalSequenceNumber;
    @XmlAttribute(name = "DisplayOrder", required = true)
    protected short displayOrder;
    @XmlAttribute(name = "IsEnabledForMultiples", required = true)
    protected boolean isEnabledForMultiples;
    @XmlAttribute(name = "IsInRunningAllowed", required = true)
    protected boolean isInRunningAllowed;
    @XmlAttribute(name = "IsManagedWhenInRunning", required = true)
    protected boolean isManagedWhenInRunning;
    @XmlAttribute(name = "IsCurrentlyInRunning", required = true)
    protected boolean isCurrentlyInRunning;
    @XmlAttribute(name = "InRunningDelaySeconds", required = true)
    protected int inRunningDelaySeconds;
    @XmlAttribute(name = "ReturnCode")
    protected Integer returnCode;
    @XmlAttribute(name = "TotalMatchedAmount")
    protected BigDecimal totalMatchedAmount;
    @XmlAttribute(name = "PlacePayout")
    protected BigDecimal placePayout;
    @XmlAttribute(name = "MatchedMarketForStake")
    protected BigDecimal matchedMarketForStake;
    @XmlAttribute(name = "MatchedMarketAgainstStake")
    protected BigDecimal matchedMarketAgainstStake;
    @XmlAttribute(name = "HomeTeamScore")
    protected Integer homeTeamScore;
    @XmlAttribute(name = "AwayTeamScore")
    protected Integer awayTeamScore;
    @XmlAttribute(name = "ScoreType")
    protected Short scoreType;

    /**
     * Gets the value of the selections property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the selections property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSelections().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SelectionTypeWithPrices }
     * 
     * 
     */
    public List<SelectionTypeWithPrices> getSelections() {
        if (selections == null) {
            selections = new ArrayList<SelectionTypeWithPrices>();
        }
        return this.selections;
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
     * Gets the value of the type property.
     * 
     */
    public short getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     */
    public void setType(short value) {
        this.type = value;
    }

    /**
     * Gets the value of the isPlayMarket property.
     * 
     */
    public boolean isIsPlayMarket() {
        return isPlayMarket;
    }

    /**
     * Sets the value of the isPlayMarket property.
     * 
     */
    public void setIsPlayMarket(boolean value) {
        this.isPlayMarket = value;
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
     * Gets the value of the numberOfWinningSelections property.
     * 
     */
    public short getNumberOfWinningSelections() {
        return numberOfWinningSelections;
    }

    /**
     * Sets the value of the numberOfWinningSelections property.
     * 
     */
    public void setNumberOfWinningSelections(short value) {
        this.numberOfWinningSelections = value;
    }

    /**
     * Gets the value of the startTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartTime(XMLGregorianCalendar value) {
        this.startTime = value;
    }

    /**
     * Gets the value of the withdrawalSequenceNumber property.
     * 
     */
    public short getWithdrawalSequenceNumber() {
        return withdrawalSequenceNumber;
    }

    /**
     * Sets the value of the withdrawalSequenceNumber property.
     * 
     */
    public void setWithdrawalSequenceNumber(short value) {
        this.withdrawalSequenceNumber = value;
    }

    /**
     * Gets the value of the displayOrder property.
     * 
     */
    public short getDisplayOrder() {
        return displayOrder;
    }

    /**
     * Sets the value of the displayOrder property.
     * 
     */
    public void setDisplayOrder(short value) {
        this.displayOrder = value;
    }

    /**
     * Gets the value of the isEnabledForMultiples property.
     * 
     */
    public boolean isIsEnabledForMultiples() {
        return isEnabledForMultiples;
    }

    /**
     * Sets the value of the isEnabledForMultiples property.
     * 
     */
    public void setIsEnabledForMultiples(boolean value) {
        this.isEnabledForMultiples = value;
    }

    /**
     * Gets the value of the isInRunningAllowed property.
     * 
     */
    public boolean isIsInRunningAllowed() {
        return isInRunningAllowed;
    }

    /**
     * Sets the value of the isInRunningAllowed property.
     * 
     */
    public void setIsInRunningAllowed(boolean value) {
        this.isInRunningAllowed = value;
    }

    /**
     * Gets the value of the isManagedWhenInRunning property.
     * 
     */
    public boolean isIsManagedWhenInRunning() {
        return isManagedWhenInRunning;
    }

    /**
     * Sets the value of the isManagedWhenInRunning property.
     * 
     */
    public void setIsManagedWhenInRunning(boolean value) {
        this.isManagedWhenInRunning = value;
    }

    /**
     * Gets the value of the isCurrentlyInRunning property.
     * 
     */
    public boolean isIsCurrentlyInRunning() {
        return isCurrentlyInRunning;
    }

    /**
     * Sets the value of the isCurrentlyInRunning property.
     * 
     */
    public void setIsCurrentlyInRunning(boolean value) {
        this.isCurrentlyInRunning = value;
    }

    /**
     * Gets the value of the inRunningDelaySeconds property.
     * 
     */
    public int getInRunningDelaySeconds() {
        return inRunningDelaySeconds;
    }

    /**
     * Sets the value of the inRunningDelaySeconds property.
     * 
     */
    public void setInRunningDelaySeconds(int value) {
        this.inRunningDelaySeconds = value;
    }

    /**
     * Gets the value of the returnCode property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getReturnCode() {
        return returnCode;
    }

    /**
     * Sets the value of the returnCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setReturnCode(Integer value) {
        this.returnCode = value;
    }

    /**
     * Gets the value of the totalMatchedAmount property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTotalMatchedAmount() {
        return totalMatchedAmount;
    }

    /**
     * Sets the value of the totalMatchedAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTotalMatchedAmount(BigDecimal value) {
        this.totalMatchedAmount = value;
    }

    /**
     * Gets the value of the placePayout property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getPlacePayout() {
        return placePayout;
    }

    /**
     * Sets the value of the placePayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setPlacePayout(BigDecimal value) {
        this.placePayout = value;
    }

    /**
     * Gets the value of the matchedMarketForStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMatchedMarketForStake() {
        return matchedMarketForStake;
    }

    /**
     * Sets the value of the matchedMarketForStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMatchedMarketForStake(BigDecimal value) {
        this.matchedMarketForStake = value;
    }

    /**
     * Gets the value of the matchedMarketAgainstStake property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMatchedMarketAgainstStake() {
        return matchedMarketAgainstStake;
    }

    /**
     * Sets the value of the matchedMarketAgainstStake property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMatchedMarketAgainstStake(BigDecimal value) {
        this.matchedMarketAgainstStake = value;
    }

    /**
     * Gets the value of the homeTeamScore property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getHomeTeamScore() {
        return homeTeamScore;
    }

    /**
     * Sets the value of the homeTeamScore property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setHomeTeamScore(Integer value) {
        this.homeTeamScore = value;
    }

    /**
     * Gets the value of the awayTeamScore property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAwayTeamScore() {
        return awayTeamScore;
    }

    /**
     * Sets the value of the awayTeamScore property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAwayTeamScore(Integer value) {
        this.awayTeamScore = value;
    }

    /**
     * Gets the value of the scoreType property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getScoreType() {
        return scoreType;
    }

    /**
     * Sets the value of the scoreType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setScoreType(Short value) {
        this.scoreType = value;
    }

}
