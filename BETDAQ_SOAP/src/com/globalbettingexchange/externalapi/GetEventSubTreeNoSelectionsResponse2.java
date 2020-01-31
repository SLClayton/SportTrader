
package com.globalbettingexchange.externalapi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetEventSubTreeNoSelectionsResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetEventSubTreeNoSelectionsResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.GlobalBettingExchange.com/ExternalAPI/}BaseResponse"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;element name="EventClassifiers" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}EventClassifierType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetEventSubTreeNoSelectionsResponse", propOrder = {
    "eventClassifiers"
})
public class GetEventSubTreeNoSelectionsResponse2
    extends BaseResponse
{

    @XmlElement(name = "EventClassifiers", required = true)
    protected List<EventClassifierType> eventClassifiers;

    /**
     * Gets the value of the eventClassifiers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the eventClassifiers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEventClassifiers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EventClassifierType }
     * 
     * 
     */
    public List<EventClassifierType> getEventClassifiers() {
        if (eventClassifiers == null) {
            eventClassifiers = new ArrayList<EventClassifierType>();
        }
        return this.eventClassifiers;
    }

}
