
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for BaseResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BaseResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ReturnStatus" type="{http://www.GlobalBettingExchange.com/ExternalAPI/}ReturnStatus"/&gt;
 *         &lt;element name="Timestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BaseResponse", propOrder = {
    "returnStatus",
    "timestamp"
})
@XmlSeeAlso({
    ListTopLevelEventsResponse2 .class,
    GetPricesResponse2 .class,
    ListMarketWithdrawalHistoryResponse2 .class,
    GetEventSubTreeWithSelectionsResponse2 .class,
    ListOrdersChangedSinceResponse2 .class,
    GetEventSubTreeNoSelectionsResponse2 .class,
    GetMarketInformationResponse2 .class,
    PlaceOrdersNoReceiptResponse2 .class,
    PlaceOrdersWithReceiptResponse2 .class,
    CancelOrdersResponse2 .class,
    CancelAllOrdersResponse2 .class,
    CancelAllOrdersOnMarketResponse2 .class,
    GetAccountBalancesResponse2 .class,
    ListAccountPostingsResponse2 .class,
    ListAccountPostingsByIdResponse2 .class,
    UpdateOrdersNoReceiptResponse2 .class,
    GetOrderDetailsResponse2 .class,
    ChangePasswordResponse2 .class,
    ListSelectionsChangedSinceResponse2 .class,
    ListBootstrapOrdersResponse2 .class,
    SuspendFromTradingResponse2 .class,
    UnsuspendFromTradingResponse2 .class,
    SuspendOrdersResponse2 .class,
    SuspendAllOrdersOnMarketResponse2 .class,
    UnsuspendOrdersResponse2 .class,
    SuspendAllOrdersResponse2 .class,
    ListBlacklistInformationResponse2 .class,
    RegisterHeartbeatResponse2 .class,
    ChangeHeartbeatRegistrationResponse2 .class,
    DeregisterHeartbeatResponse2 .class,
    PulseResponse2 .class,
    GetOddsLadderResponse2 .class,
    GetCurrentSelectionSequenceNumberResponse2 .class,
    ListSelectionTradesResponse2 .class,
    GetSPEnabledMarketsInformationResponse2 .class,
    ListTaggedValuesResponse2 .class
})
public abstract class BaseResponse {

    @XmlElement(name = "ReturnStatus", required = true)
    protected ReturnStatus returnStatus;
    @XmlElement(name = "Timestamp", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timestamp;

    /**
     * Gets the value of the returnStatus property.
     * 
     * @return
     *     possible object is
     *     {@link ReturnStatus }
     *     
     */
    public ReturnStatus getReturnStatus() {
        return returnStatus;
    }

    /**
     * Sets the value of the returnStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReturnStatus }
     *     
     */
    public void setReturnStatus(ReturnStatus value) {
        this.returnStatus = value;
    }

    /**
     * Gets the value of the timestamp property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimestamp(XMLGregorianCalendar value) {
        this.timestamp = value;
    }

}
