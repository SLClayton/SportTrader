
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="getOddsLadderRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}GetOddsLadderRequest" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getOddsLadderRequest"
})
@XmlRootElement(name = "GetOddsLadder")
public class GetOddsLadder {

    protected GetOddsLadderRequest getOddsLadderRequest;

    /**
     * Gets the value of the getOddsLadderRequest property.
     * 
     * @return
     *     possible object is
     *     {@link GetOddsLadderRequest }
     *     
     */
    public GetOddsLadderRequest getGetOddsLadderRequest() {
        return getOddsLadderRequest;
    }

    /**
     * Sets the value of the getOddsLadderRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetOddsLadderRequest }
     *     
     */
    public void setGetOddsLadderRequest(GetOddsLadderRequest value) {
        this.getOddsLadderRequest = value;
    }

}
