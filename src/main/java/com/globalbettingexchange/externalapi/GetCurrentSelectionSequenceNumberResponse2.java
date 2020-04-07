
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetCurrentSelectionSequenceNumberResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetCurrentSelectionSequenceNumberResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.GlobalBettingExchange.com/ExternalAPI/}BaseResponse"&gt;
 *       &lt;attribute name="SelectionSequenceNumber" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetCurrentSelectionSequenceNumberResponse")
public class GetCurrentSelectionSequenceNumberResponse2
    extends BaseResponse
{

    @XmlAttribute(name = "SelectionSequenceNumber")
    protected Long selectionSequenceNumber;

    /**
     * Gets the value of the selectionSequenceNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSelectionSequenceNumber() {
        return selectionSequenceNumber;
    }

    /**
     * Sets the value of the selectionSequenceNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSelectionSequenceNumber(Long value) {
        this.selectionSequenceNumber = value;
    }

}
