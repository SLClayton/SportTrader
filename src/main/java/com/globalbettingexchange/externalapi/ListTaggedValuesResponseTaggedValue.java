
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * A type representing a TaggedValue
 * 
 * <p>Java class for ListTaggedValuesResponseTaggedValue complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListTaggedValuesResponseTaggedValue"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="TaggedValues" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ListTaggedValuesResponseInnerTaggedValue"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Identifier" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListTaggedValuesResponseTaggedValue", propOrder = {
    "taggedValues"
})
public class ListTaggedValuesResponseTaggedValue {

    @XmlElement(name = "TaggedValues")
    protected List<ListTaggedValuesResponseInnerTaggedValue> taggedValues;
    @XmlAttribute(name = "Identifier", required = true)
    protected long identifier;

    /**
     * Gets the value of the taggedValues property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the taggedValues property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTaggedValues().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ListTaggedValuesResponseInnerTaggedValue }
     * 
     * 
     */
    public List<ListTaggedValuesResponseInnerTaggedValue> getTaggedValues() {
        if (taggedValues == null) {
            taggedValues = new ArrayList<ListTaggedValuesResponseInnerTaggedValue>();
        }
        return this.taggedValues;
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

}
