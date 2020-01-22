
package com.globalbettingexchange.externalapi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EventClassifierType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EventClassifierType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="EventClassifiers" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}EventClassifierType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Markets" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}MarketType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Id" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="Name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="DisplayOrder" use="required" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *       &lt;attribute name="IsEnabledForMultiples" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="ParentId" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventClassifierType", propOrder = {
    "eventClassifiers",
    "markets"
})
public class EventClassifierType {

    @XmlElement(name = "EventClassifiers")
    protected List<EventClassifierType> eventClassifiers;
    @XmlElement(name = "Markets")
    protected List<MarketType> markets;
    @XmlAttribute(name = "Id", required = true)
    protected long id;
    @XmlAttribute(name = "Name", required = true)
    protected String name;
    @XmlAttribute(name = "DisplayOrder", required = true)
    protected short displayOrder;
    @XmlAttribute(name = "IsEnabledForMultiples", required = true)
    protected boolean isEnabledForMultiples;
    @XmlAttribute(name = "ParentId")
    protected Long parentId;

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
     * Gets the value of the parentId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * Sets the value of the parentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setParentId(Long value) {
        this.parentId = value;
    }

}
