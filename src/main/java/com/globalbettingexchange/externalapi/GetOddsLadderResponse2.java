
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for GetOddsLadderResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetOddsLadderResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.GlobalBettingExchange.com/ExternalAPI/}BaseResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Ladder" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetOddsLadderResponseItem" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetOddsLadderResponse", propOrder = {
    "ladder"
})
public class GetOddsLadderResponse2
    extends BaseResponse
{

    @XmlElement(name = "Ladder")
    protected List<GetOddsLadderResponseItem> ladder;

    /**
     * Gets the value of the ladder property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ladder property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLadder().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetOddsLadderResponseItem }
     * 
     * 
     */
    public List<GetOddsLadderResponseItem> getLadder() {
        if (ladder == null) {
            ladder = new ArrayList<GetOddsLadderResponseItem>();
        }
        return this.ladder;
    }

}
