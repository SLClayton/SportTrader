
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ListBootstrapOrdersRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListBootstrapOrdersRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SequenceNumber" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="wantSettledOrdersOnUnsettledMarkets" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListBootstrapOrdersRequest", propOrder = {
    "sequenceNumber",
    "wantSettledOrdersOnUnsettledMarkets"
})
public class ListBootstrapOrdersRequest {

    @XmlElement(name = "SequenceNumber")
    protected long sequenceNumber;
    protected Boolean wantSettledOrdersOnUnsettledMarkets;

    /**
     * Gets the value of the sequenceNumber property.
     * 
     */
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the value of the sequenceNumber property.
     * 
     */
    public void setSequenceNumber(long value) {
        this.sequenceNumber = value;
    }

    /**
     * Gets the value of the wantSettledOrdersOnUnsettledMarkets property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWantSettledOrdersOnUnsettledMarkets() {
        return wantSettledOrdersOnUnsettledMarkets;
    }

    /**
     * Sets the value of the wantSettledOrdersOnUnsettledMarkets property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWantSettledOrdersOnUnsettledMarkets(Boolean value) {
        this.wantSettledOrdersOnUnsettledMarkets = value;
    }

}
