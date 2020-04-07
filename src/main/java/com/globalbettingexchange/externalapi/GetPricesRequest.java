
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for GetPricesRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetPricesRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;element name="MarketIds" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="ThresholdAmount" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="NumberForPricesRequired" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="NumberAgainstPricesRequired" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="WantMarketMatchedAmount" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="WantSelectionsMatchedAmounts" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="WantSelectionMatchedDetails" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetPricesRequest", propOrder = {
    "marketIds"
})
public class GetPricesRequest {

    @XmlElement(name = "MarketIds", type = Long.class)
    protected List<Long> marketIds;
    @XmlAttribute(name = "ThresholdAmount", required = true)
    protected BigDecimal thresholdAmount;
    @XmlAttribute(name = "NumberForPricesRequired", required = true)
    protected int numberForPricesRequired;
    @XmlAttribute(name = "NumberAgainstPricesRequired", required = true)
    protected int numberAgainstPricesRequired;
    @XmlAttribute(name = "WantMarketMatchedAmount")
    protected Boolean wantMarketMatchedAmount;
    @XmlAttribute(name = "WantSelectionsMatchedAmounts")
    protected Boolean wantSelectionsMatchedAmounts;
    @XmlAttribute(name = "WantSelectionMatchedDetails")
    protected Boolean wantSelectionMatchedDetails;

    /**
     * Gets the value of the marketIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the marketIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMarketIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getMarketIds() {
        if (marketIds == null) {
            marketIds = new ArrayList<Long>();
        }
        return this.marketIds;
    }

    /**
     * Gets the value of the thresholdAmount property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getThresholdAmount() {
        return thresholdAmount;
    }

    /**
     * Sets the value of the thresholdAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setThresholdAmount(BigDecimal value) {
        this.thresholdAmount = value;
    }

    /**
     * Gets the value of the numberForPricesRequired property.
     * 
     */
    public int getNumberForPricesRequired() {
        return numberForPricesRequired;
    }

    /**
     * Sets the value of the numberForPricesRequired property.
     * 
     */
    public void setNumberForPricesRequired(int value) {
        this.numberForPricesRequired = value;
    }

    /**
     * Gets the value of the numberAgainstPricesRequired property.
     * 
     */
    public int getNumberAgainstPricesRequired() {
        return numberAgainstPricesRequired;
    }

    /**
     * Sets the value of the numberAgainstPricesRequired property.
     * 
     */
    public void setNumberAgainstPricesRequired(int value) {
        this.numberAgainstPricesRequired = value;
    }

    /**
     * Gets the value of the wantMarketMatchedAmount property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWantMarketMatchedAmount() {
        return wantMarketMatchedAmount;
    }

    /**
     * Sets the value of the wantMarketMatchedAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWantMarketMatchedAmount(Boolean value) {
        this.wantMarketMatchedAmount = value;
    }

    /**
     * Gets the value of the wantSelectionsMatchedAmounts property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWantSelectionsMatchedAmounts() {
        return wantSelectionsMatchedAmounts;
    }

    /**
     * Sets the value of the wantSelectionsMatchedAmounts property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWantSelectionsMatchedAmounts(Boolean value) {
        this.wantSelectionsMatchedAmounts = value;
    }

    /**
     * Gets the value of the wantSelectionMatchedDetails property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWantSelectionMatchedDetails() {
        return wantSelectionMatchedDetails;
    }

    /**
     * Sets the value of the wantSelectionMatchedDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWantSelectionMatchedDetails(Boolean value) {
        this.wantSelectionMatchedDetails = value;
    }

}
