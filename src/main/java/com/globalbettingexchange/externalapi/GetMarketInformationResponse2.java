
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for GetMarketInformationResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetMarketInformationResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.GlobalBettingExchange.com/ExternalAPI/}BaseResponse"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;element name="Markets" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}MarketType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetMarketInformationResponse", propOrder = {
    "markets"
})
public class GetMarketInformationResponse2
    extends BaseResponse
{

    @XmlElement(name = "Markets", required = true)
    protected List<MarketType> markets;

    /**
     * Gets the value of the markets property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the markets property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMarkets().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MarketType }
     * 
     * 
     */
    public List<MarketType> getMarkets() {
        if (markets == null) {
            markets = new ArrayList<MarketType>();
        }
        return this.markets;
    }

}
