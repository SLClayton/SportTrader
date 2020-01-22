
package com.globalbettingexchange.externalapi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ListTaggedValuesRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListTaggedValuesRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;element name="Entities" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListTaggedValuesRequestEntity" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="WantDescendents" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListTaggedValuesRequest", propOrder = {
    "entities"
})
public class ListTaggedValuesRequest {

    @XmlElement(name = "Entities", required = true)
    protected List<ListTaggedValuesRequestEntity> entities;
    @XmlAttribute(name = "WantDescendents")
    protected Boolean wantDescendents;

    /**
     * Gets the value of the entities property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the entities property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEntities().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ListTaggedValuesRequestEntity }
     * 
     * 
     */
    public List<ListTaggedValuesRequestEntity> getEntities() {
        if (entities == null) {
            entities = new ArrayList<ListTaggedValuesRequestEntity>();
        }
        return this.entities;
    }

    /**
     * Gets the value of the wantDescendents property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWantDescendents() {
        return wantDescendents;
    }

    /**
     * Sets the value of the wantDescendents property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWantDescendents(Boolean value) {
        this.wantDescendents = value;
    }

}
