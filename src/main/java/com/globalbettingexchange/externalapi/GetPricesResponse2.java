
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for GetPricesResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetPricesResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.GlobalBettingExchange.com/ExternalAPI/}BaseResponse"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;element name="MarketPrices" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}MarketTypeWithPrices"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetPricesResponse", propOrder = {
    "marketPrices"
})
public class GetPricesResponse2
    extends BaseResponse
{

    @XmlElement(name = "MarketPrices", required = true)
    protected List<MarketTypeWithPrices> marketPrices;

    /**
     * Gets the value of the marketPrices property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the marketPrices property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMarketPrices().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MarketTypeWithPrices }
     * 
     * 
     */
    public List<MarketTypeWithPrices> getMarketPrices() {
        if (marketPrices == null) {
            marketPrices = new ArrayList<MarketTypeWithPrices>();
        }
        return this.marketPrices;
    }

}
