
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ListSelectionsChangedSinceRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListSelectionsChangedSinceRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="SelectionSequenceNumber" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListSelectionsChangedSinceRequest")
public class ListSelectionsChangedSinceRequest {

    @XmlAttribute(name = "SelectionSequenceNumber", required = true)
    protected long selectionSequenceNumber;

    /**
     * Gets the value of the selectionSequenceNumber property.
     * 
     */
    public long getSelectionSequenceNumber() {
        return selectionSequenceNumber;
    }

    /**
     * Sets the value of the selectionSequenceNumber property.
     * 
     */
    public void setSelectionSequenceNumber(long value) {
        this.selectionSequenceNumber = value;
    }

}
