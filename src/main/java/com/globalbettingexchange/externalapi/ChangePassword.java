
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
 *         &lt;element name="changePasswordRequest" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ChangePasswordRequest" minOccurs="0"/&gt;
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
    "changePasswordRequest"
})
@XmlRootElement(name = "ChangePassword")
public class ChangePassword {

    protected ChangePasswordRequest changePasswordRequest;

    /**
     * Gets the value of the changePasswordRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ChangePasswordRequest }
     *     
     */
    public ChangePasswordRequest getChangePasswordRequest() {
        return changePasswordRequest;
    }

    /**
     * Sets the value of the changePasswordRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChangePasswordRequest }
     *     
     */
    public void setChangePasswordRequest(ChangePasswordRequest value) {
        this.changePasswordRequest = value;
    }

}
