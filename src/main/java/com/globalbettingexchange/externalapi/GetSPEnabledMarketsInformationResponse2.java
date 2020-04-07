
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for GetSPEnabledMarketsInformationResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetSPEnabledMarketsInformationResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.GlobalBettingExchange.com/ExternalAPI/}BaseResponse"&gt;
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="SPEnabledEvent"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="MarketTypeIds"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="MarketTypeId" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" maxOccurs="unbounded"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="eventId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetSPEnabledMarketsInformationResponse", propOrder = {
    "spEnabledEvent"
})
public class GetSPEnabledMarketsInformationResponse2
    extends BaseResponse
{

    @XmlElement(name = "SPEnabledEvent")
    protected List<SPEnabledEvent> spEnabledEvent;

    /**
     * Gets the value of the spEnabledEvent property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the spEnabledEvent property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSPEnabledEvent().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetSPEnabledMarketsInformationResponse2 .SPEnabledEvent }
     *
     *
     */
    public List<SPEnabledEvent> getSPEnabledEvent() {
        if (spEnabledEvent == null) {
            spEnabledEvent = new ArrayList<SPEnabledEvent>();
        }
        return this.spEnabledEvent;
    }


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
     *         &lt;element name="MarketTypeIds"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="MarketTypeId" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" maxOccurs="unbounded"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="eventId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "marketTypeIds"
    })
    public static class SPEnabledEvent {

        @XmlElement(name = "MarketTypeIds", required = true)
        protected MarketTypeIds marketTypeIds;
        @XmlAttribute(name = "eventId", required = true)
        protected long eventId;

        /**
         * Gets the value of the marketTypeIds property.
         *
         * @return
         *     possible object is
         *     {@link GetSPEnabledMarketsInformationResponse2 .SPEnabledEvent.MarketTypeIds }
         *
         */
        public MarketTypeIds getMarketTypeIds() {
            return marketTypeIds;
        }

        /**
         * Sets the value of the marketTypeIds property.
         *
         * @param value
         *     allowed object is
         *     {@link GetSPEnabledMarketsInformationResponse2 .SPEnabledEvent.MarketTypeIds }
         *
         */
        public void setMarketTypeIds(MarketTypeIds value) {
            this.marketTypeIds = value;
        }

        /**
         * Gets the value of the eventId property.
         * 
         */
        public long getEventId() {
            return eventId;
        }

        /**
         * Sets the value of the eventId property.
         * 
         */
        public void setEventId(long value) {
            this.eventId = value;
        }


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
         *         &lt;element name="MarketTypeId" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" maxOccurs="unbounded"/&gt;
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
            "marketTypeId"
        })
        public static class MarketTypeIds {

            @XmlElement(name = "MarketTypeId", type = Short.class)
            @XmlSchemaType(name = "unsignedByte")
            protected List<Short> marketTypeId;

            /**
             * Gets the value of the marketTypeId property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the marketTypeId property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getMarketTypeId().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Short }
             * 
             * 
             */
            public List<Short> getMarketTypeId() {
                if (marketTypeId == null) {
                    marketTypeId = new ArrayList<Short>();
                }
                return this.marketTypeId;
            }

        }

    }

}
