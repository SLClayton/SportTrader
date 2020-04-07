
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * A type representing an Entity which is being requested
 * 
 * <p>Java class for ListTaggedValuesRequestEntity complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListTaggedValuesRequestEntity"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;element name="Names" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Identifier" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="EntityType" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListTaggedValuesRequestEntity", propOrder = {
    "names"
})
public class ListTaggedValuesRequestEntity {

    @XmlElement(name = "Names", required = true)
    protected List<String> names;
    @XmlAttribute(name = "Identifier", required = true)
    protected long identifier;
    @XmlAttribute(name = "EntityType", required = true)
    protected int entityType;

    /**
     * Gets the value of the names property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the names property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getNames() {
        if (names == null) {
            names = new ArrayList<String>();
        }
        return this.names;
    }

    /**
     * Gets the value of the identifier property.
     * 
     */
    public long getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     * 
     */
    public void setIdentifier(long value) {
        this.identifier = value;
    }

    /**
     * Gets the value of the entityType property.
     * 
     */
    public int getEntityType() {
        return entityType;
    }

    /**
     * Sets the value of the entityType property.
     * 
     */
    public void setEntityType(int value) {
        this.entityType = value;
    }

}
