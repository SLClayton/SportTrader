
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetOddsLadderRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetOddsLadderRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="PriceFormat" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetOddsLadderRequest")
public class GetOddsLadderRequest {

    @XmlAttribute(name = "PriceFormat")
    @XmlSchemaType(name = "unsignedByte")
    protected Short priceFormat;

    /**
     * Gets the value of the priceFormat property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getPriceFormat() {
        return priceFormat;
    }

    /**
     * Sets the value of the priceFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setPriceFormat(Short value) {
        this.priceFormat = value;
    }

}
