
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="ChangePasswordResult" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ChangePasswordResponse" minOccurs="0"/&gt;
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
    "changePasswordResult"
})
@XmlRootElement(name = "ChangePasswordResponse")
public class ChangePasswordResponse {

    @XmlElement(name = "ChangePasswordResult")
    protected ChangePasswordResponse2 changePasswordResult;

    /**
     * Gets the value of the changePasswordResult property.
     * 
     * @return
     *     possible object is
     *     {@link ChangePasswordResponse2 }
     *     
     */
    public ChangePasswordResponse2 getChangePasswordResult() {
        return changePasswordResult;
    }

    /**
     * Sets the value of the changePasswordResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChangePasswordResponse2 }
     *     
     */
    public void setChangePasswordResult(ChangePasswordResponse2 value) {
        this.changePasswordResult = value;
    }

}
