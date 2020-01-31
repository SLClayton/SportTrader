
package com.globalbettingexchange.externalapi;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.globalbettingexchange.externalapi package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ExternalApiHeader_QNAME = new QName("http://www.GlobalBettingExchange.com/ExternalAPI/", "ExternalApiHeader");
    private final static QName _SelectionTypeWithPricesForSidePrices_QNAME = new QName("http://www.GlobalBettingExchange.com/ExternalAPI/", "ForSidePrices");
    private final static QName _SelectionTypeWithPricesAgainstSidePrices_QNAME = new QName("http://www.GlobalBettingExchange.com/ExternalAPI/", "AgainstSidePrices");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.globalbettingexchange.externalapi
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetSPEnabledMarketsInformationResponse2 }
     * 
     */
    public GetSPEnabledMarketsInformationResponse2 createGetSPEnabledMarketsInformationResponse2() {
        return new GetSPEnabledMarketsInformationResponse2();
    }

    /**
     * Create an instance of {@link GetSPEnabledMarketsInformationResponse2 .SPEnabledEvent }
     * 
     */
    public GetSPEnabledMarketsInformationResponse2 .SPEnabledEvent createGetSPEnabledMarketsInformationResponse2SPEnabledEvent() {
        return new GetSPEnabledMarketsInformationResponse2 .SPEnabledEvent();
    }

    /**
     * Create an instance of {@link ListBootstrapOrdersResponse2 }
     * 
     */
    public ListBootstrapOrdersResponse2 createListBootstrapOrdersResponse2() {
        return new ListBootstrapOrdersResponse2();
    }

    /**
     * Create an instance of {@link GetOrderDetailsResponse2 }
     * 
     */
    public GetOrderDetailsResponse2 createGetOrderDetailsResponse2() {
        return new GetOrderDetailsResponse2();
    }

    /**
     * Create an instance of {@link UpdateOrdersNoReceiptResponse2 }
     * 
     */
    public UpdateOrdersNoReceiptResponse2 createUpdateOrdersNoReceiptResponse2() {
        return new UpdateOrdersNoReceiptResponse2();
    }

    /**
     * Create an instance of {@link UpdateOrdersNoReceiptRequest }
     * 
     */
    public UpdateOrdersNoReceiptRequest createUpdateOrdersNoReceiptRequest() {
        return new UpdateOrdersNoReceiptRequest();
    }

    /**
     * Create an instance of {@link ListAccountPostingsByIdResponse2 }
     * 
     */
    public ListAccountPostingsByIdResponse2 createListAccountPostingsByIdResponse2() {
        return new ListAccountPostingsByIdResponse2();
    }

    /**
     * Create an instance of {@link ListAccountPostingsResponse2 }
     * 
     */
    public ListAccountPostingsResponse2 createListAccountPostingsResponse2() {
        return new ListAccountPostingsResponse2();
    }

    /**
     * Create an instance of {@link CancelAllOrdersResponse2 }
     * 
     */
    public CancelAllOrdersResponse2 createCancelAllOrdersResponse2() {
        return new CancelAllOrdersResponse2();
    }

    /**
     * Create an instance of {@link CancelOrdersResponse2 }
     * 
     */
    public CancelOrdersResponse2 createCancelOrdersResponse2() {
        return new CancelOrdersResponse2();
    }

    /**
     * Create an instance of {@link PlaceOrdersWithReceiptResponse2 }
     * 
     */
    public PlaceOrdersWithReceiptResponse2 createPlaceOrdersWithReceiptResponse2() {
        return new PlaceOrdersWithReceiptResponse2();
    }

    /**
     * Create an instance of {@link PlaceOrdersWithReceiptRequest }
     * 
     */
    public PlaceOrdersWithReceiptRequest createPlaceOrdersWithReceiptRequest() {
        return new PlaceOrdersWithReceiptRequest();
    }

    /**
     * Create an instance of {@link PlaceOrdersNoReceiptResponse2 }
     * 
     */
    public PlaceOrdersNoReceiptResponse2 createPlaceOrdersNoReceiptResponse2() {
        return new PlaceOrdersNoReceiptResponse2();
    }

    /**
     * Create an instance of {@link PlaceOrdersNoReceiptRequest }
     * 
     */
    public PlaceOrdersNoReceiptRequest createPlaceOrdersNoReceiptRequest() {
        return new PlaceOrdersNoReceiptRequest();
    }

    /**
     * Create an instance of {@link ListOrdersChangedSinceResponse2 }
     * 
     */
    public ListOrdersChangedSinceResponse2 createListOrdersChangedSinceResponse2() {
        return new ListOrdersChangedSinceResponse2();
    }

    /**
     * Create an instance of {@link ExternalApiHeader }
     * 
     */
    public ExternalApiHeader createExternalApiHeader() {
        return new ExternalApiHeader();
    }

    /**
     * Create an instance of {@link ListTopLevelEvents }
     * 
     */
    public ListTopLevelEvents createListTopLevelEvents() {
        return new ListTopLevelEvents();
    }

    /**
     * Create an instance of {@link ListTopLevelEventsRequest }
     * 
     */
    public ListTopLevelEventsRequest createListTopLevelEventsRequest() {
        return new ListTopLevelEventsRequest();
    }

    /**
     * Create an instance of {@link ListTopLevelEventsResponse }
     * 
     */
    public ListTopLevelEventsResponse createListTopLevelEventsResponse() {
        return new ListTopLevelEventsResponse();
    }

    /**
     * Create an instance of {@link ListTopLevelEventsResponse2 }
     * 
     */
    public ListTopLevelEventsResponse2 createListTopLevelEventsResponse2() {
        return new ListTopLevelEventsResponse2();
    }

    /**
     * Create an instance of {@link GetPrices }
     * 
     */
    public GetPrices createGetPrices() {
        return new GetPrices();
    }

    /**
     * Create an instance of {@link GetPricesRequest }
     * 
     */
    public GetPricesRequest createGetPricesRequest() {
        return new GetPricesRequest();
    }

    /**
     * Create an instance of {@link GetPricesResponse }
     * 
     */
    public GetPricesResponse createGetPricesResponse() {
        return new GetPricesResponse();
    }

    /**
     * Create an instance of {@link GetPricesResponse2 }
     * 
     */
    public GetPricesResponse2 createGetPricesResponse2() {
        return new GetPricesResponse2();
    }

    /**
     * Create an instance of {@link ListMarketWithdrawalHistory }
     * 
     */
    public ListMarketWithdrawalHistory createListMarketWithdrawalHistory() {
        return new ListMarketWithdrawalHistory();
    }

    /**
     * Create an instance of {@link ListMarketWithdrawalHistoryRequest }
     * 
     */
    public ListMarketWithdrawalHistoryRequest createListMarketWithdrawalHistoryRequest() {
        return new ListMarketWithdrawalHistoryRequest();
    }

    /**
     * Create an instance of {@link ListMarketWithdrawalHistoryResponse }
     * 
     */
    public ListMarketWithdrawalHistoryResponse createListMarketWithdrawalHistoryResponse() {
        return new ListMarketWithdrawalHistoryResponse();
    }

    /**
     * Create an instance of {@link ListMarketWithdrawalHistoryResponse2 }
     * 
     */
    public ListMarketWithdrawalHistoryResponse2 createListMarketWithdrawalHistoryResponse2() {
        return new ListMarketWithdrawalHistoryResponse2();
    }

    /**
     * Create an instance of {@link GetEventSubTreeWithSelections }
     * 
     */
    public GetEventSubTreeWithSelections createGetEventSubTreeWithSelections() {
        return new GetEventSubTreeWithSelections();
    }

    /**
     * Create an instance of {@link GetEventSubTreeWithSelectionsRequest }
     * 
     */
    public GetEventSubTreeWithSelectionsRequest createGetEventSubTreeWithSelectionsRequest() {
        return new GetEventSubTreeWithSelectionsRequest();
    }

    /**
     * Create an instance of {@link GetEventSubTreeWithSelectionsResponse }
     * 
     */
    public GetEventSubTreeWithSelectionsResponse createGetEventSubTreeWithSelectionsResponse() {
        return new GetEventSubTreeWithSelectionsResponse();
    }

    /**
     * Create an instance of {@link GetEventSubTreeWithSelectionsResponse2 }
     * 
     */
    public GetEventSubTreeWithSelectionsResponse2 createGetEventSubTreeWithSelectionsResponse2() {
        return new GetEventSubTreeWithSelectionsResponse2();
    }

    /**
     * Create an instance of {@link ListOrdersChangedSince }
     * 
     */
    public ListOrdersChangedSince createListOrdersChangedSince() {
        return new ListOrdersChangedSince();
    }

    /**
     * Create an instance of {@link ListOrdersChangedSinceRequest }
     * 
     */
    public ListOrdersChangedSinceRequest createListOrdersChangedSinceRequest() {
        return new ListOrdersChangedSinceRequest();
    }

    /**
     * Create an instance of {@link ListOrdersChangedSinceResponse }
     * 
     */
    public ListOrdersChangedSinceResponse createListOrdersChangedSinceResponse() {
        return new ListOrdersChangedSinceResponse();
    }

    /**
     * Create an instance of {@link GetEventSubTreeNoSelections }
     * 
     */
    public GetEventSubTreeNoSelections createGetEventSubTreeNoSelections() {
        return new GetEventSubTreeNoSelections();
    }

    /**
     * Create an instance of {@link GetEventSubTreeNoSelectionsRequest }
     * 
     */
    public GetEventSubTreeNoSelectionsRequest createGetEventSubTreeNoSelectionsRequest() {
        return new GetEventSubTreeNoSelectionsRequest();
    }

    /**
     * Create an instance of {@link GetEventSubTreeNoSelectionsResponse }
     * 
     */
    public GetEventSubTreeNoSelectionsResponse createGetEventSubTreeNoSelectionsResponse() {
        return new GetEventSubTreeNoSelectionsResponse();
    }

    /**
     * Create an instance of {@link GetEventSubTreeNoSelectionsResponse2 }
     * 
     */
    public GetEventSubTreeNoSelectionsResponse2 createGetEventSubTreeNoSelectionsResponse2() {
        return new GetEventSubTreeNoSelectionsResponse2();
    }

    /**
     * Create an instance of {@link GetMarketInformation }
     * 
     */
    public GetMarketInformation createGetMarketInformation() {
        return new GetMarketInformation();
    }

    /**
     * Create an instance of {@link GetMarketInformationRequest }
     * 
     */
    public GetMarketInformationRequest createGetMarketInformationRequest() {
        return new GetMarketInformationRequest();
    }

    /**
     * Create an instance of {@link GetMarketInformationResponse }
     * 
     */
    public GetMarketInformationResponse createGetMarketInformationResponse() {
        return new GetMarketInformationResponse();
    }

    /**
     * Create an instance of {@link GetMarketInformationResponse2 }
     * 
     */
    public GetMarketInformationResponse2 createGetMarketInformationResponse2() {
        return new GetMarketInformationResponse2();
    }

    /**
     * Create an instance of {@link PlaceOrdersNoReceipt }
     * 
     */
    public PlaceOrdersNoReceipt createPlaceOrdersNoReceipt() {
        return new PlaceOrdersNoReceipt();
    }

    /**
     * Create an instance of {@link PlaceOrdersNoReceiptResponse }
     * 
     */
    public PlaceOrdersNoReceiptResponse createPlaceOrdersNoReceiptResponse() {
        return new PlaceOrdersNoReceiptResponse();
    }

    /**
     * Create an instance of {@link PlaceOrdersWithReceipt }
     * 
     */
    public PlaceOrdersWithReceipt createPlaceOrdersWithReceipt() {
        return new PlaceOrdersWithReceipt();
    }

    /**
     * Create an instance of {@link PlaceOrdersWithReceiptResponse }
     * 
     */
    public PlaceOrdersWithReceiptResponse createPlaceOrdersWithReceiptResponse() {
        return new PlaceOrdersWithReceiptResponse();
    }

    /**
     * Create an instance of {@link CancelOrders }
     * 
     */
    public CancelOrders createCancelOrders() {
        return new CancelOrders();
    }

    /**
     * Create an instance of {@link CancelOrdersRequest }
     * 
     */
    public CancelOrdersRequest createCancelOrdersRequest() {
        return new CancelOrdersRequest();
    }

    /**
     * Create an instance of {@link CancelOrdersResponse }
     * 
     */
    public CancelOrdersResponse createCancelOrdersResponse() {
        return new CancelOrdersResponse();
    }

    /**
     * Create an instance of {@link CancelAllOrders }
     * 
     */
    public CancelAllOrders createCancelAllOrders() {
        return new CancelAllOrders();
    }

    /**
     * Create an instance of {@link CancelAllOrdersRequest }
     * 
     */
    public CancelAllOrdersRequest createCancelAllOrdersRequest() {
        return new CancelAllOrdersRequest();
    }

    /**
     * Create an instance of {@link CancelAllOrdersResponse }
     * 
     */
    public CancelAllOrdersResponse createCancelAllOrdersResponse() {
        return new CancelAllOrdersResponse();
    }

    /**
     * Create an instance of {@link CancelAllOrdersOnMarket }
     * 
     */
    public CancelAllOrdersOnMarket createCancelAllOrdersOnMarket() {
        return new CancelAllOrdersOnMarket();
    }

    /**
     * Create an instance of {@link CancelAllOrdersOnMarketRequest }
     * 
     */
    public CancelAllOrdersOnMarketRequest createCancelAllOrdersOnMarketRequest() {
        return new CancelAllOrdersOnMarketRequest();
    }

    /**
     * Create an instance of {@link CancelAllOrdersOnMarketResponse }
     * 
     */
    public CancelAllOrdersOnMarketResponse createCancelAllOrdersOnMarketResponse() {
        return new CancelAllOrdersOnMarketResponse();
    }

    /**
     * Create an instance of {@link CancelAllOrdersOnMarketResponse2 }
     * 
     */
    public CancelAllOrdersOnMarketResponse2 createCancelAllOrdersOnMarketResponse2() {
        return new CancelAllOrdersOnMarketResponse2();
    }

    /**
     * Create an instance of {@link GetAccountBalances }
     * 
     */
    public GetAccountBalances createGetAccountBalances() {
        return new GetAccountBalances();
    }

    /**
     * Create an instance of {@link GetAccountBalancesRequest }
     * 
     */
    public GetAccountBalancesRequest createGetAccountBalancesRequest() {
        return new GetAccountBalancesRequest();
    }

    /**
     * Create an instance of {@link GetAccountBalancesResponse }
     * 
     */
    public GetAccountBalancesResponse createGetAccountBalancesResponse() {
        return new GetAccountBalancesResponse();
    }

    /**
     * Create an instance of {@link GetAccountBalancesResponse2 }
     * 
     */
    public GetAccountBalancesResponse2 createGetAccountBalancesResponse2() {
        return new GetAccountBalancesResponse2();
    }

    /**
     * Create an instance of {@link ListAccountPostings }
     * 
     */
    public ListAccountPostings createListAccountPostings() {
        return new ListAccountPostings();
    }

    /**
     * Create an instance of {@link ListAccountPostingsRequest }
     * 
     */
    public ListAccountPostingsRequest createListAccountPostingsRequest() {
        return new ListAccountPostingsRequest();
    }

    /**
     * Create an instance of {@link ListAccountPostingsResponse }
     * 
     */
    public ListAccountPostingsResponse createListAccountPostingsResponse() {
        return new ListAccountPostingsResponse();
    }

    /**
     * Create an instance of {@link ListAccountPostingsById }
     * 
     */
    public ListAccountPostingsById createListAccountPostingsById() {
        return new ListAccountPostingsById();
    }

    /**
     * Create an instance of {@link ListAccountPostingsByIdRequest }
     * 
     */
    public ListAccountPostingsByIdRequest createListAccountPostingsByIdRequest() {
        return new ListAccountPostingsByIdRequest();
    }

    /**
     * Create an instance of {@link ListAccountPostingsByIdResponse }
     * 
     */
    public ListAccountPostingsByIdResponse createListAccountPostingsByIdResponse() {
        return new ListAccountPostingsByIdResponse();
    }

    /**
     * Create an instance of {@link UpdateOrdersNoReceipt }
     * 
     */
    public UpdateOrdersNoReceipt createUpdateOrdersNoReceipt() {
        return new UpdateOrdersNoReceipt();
    }

    /**
     * Create an instance of {@link UpdateOrdersNoReceiptResponse }
     * 
     */
    public UpdateOrdersNoReceiptResponse createUpdateOrdersNoReceiptResponse() {
        return new UpdateOrdersNoReceiptResponse();
    }

    /**
     * Create an instance of {@link GetOrderDetails }
     * 
     */
    public GetOrderDetails createGetOrderDetails() {
        return new GetOrderDetails();
    }

    /**
     * Create an instance of {@link GetOrderDetailsRequest }
     * 
     */
    public GetOrderDetailsRequest createGetOrderDetailsRequest() {
        return new GetOrderDetailsRequest();
    }

    /**
     * Create an instance of {@link GetOrderDetailsResponse }
     * 
     */
    public GetOrderDetailsResponse createGetOrderDetailsResponse() {
        return new GetOrderDetailsResponse();
    }

    /**
     * Create an instance of {@link ChangePassword }
     * 
     */
    public ChangePassword createChangePassword() {
        return new ChangePassword();
    }

    /**
     * Create an instance of {@link ChangePasswordRequest }
     * 
     */
    public ChangePasswordRequest createChangePasswordRequest() {
        return new ChangePasswordRequest();
    }

    /**
     * Create an instance of {@link ChangePasswordResponse }
     * 
     */
    public ChangePasswordResponse createChangePasswordResponse() {
        return new ChangePasswordResponse();
    }

    /**
     * Create an instance of {@link ChangePasswordResponse2 }
     * 
     */
    public ChangePasswordResponse2 createChangePasswordResponse2() {
        return new ChangePasswordResponse2();
    }

    /**
     * Create an instance of {@link ListSelectionsChangedSince }
     * 
     */
    public ListSelectionsChangedSince createListSelectionsChangedSince() {
        return new ListSelectionsChangedSince();
    }

    /**
     * Create an instance of {@link ListSelectionsChangedSinceRequest }
     * 
     */
    public ListSelectionsChangedSinceRequest createListSelectionsChangedSinceRequest() {
        return new ListSelectionsChangedSinceRequest();
    }

    /**
     * Create an instance of {@link ListSelectionsChangedSinceResponse }
     * 
     */
    public ListSelectionsChangedSinceResponse createListSelectionsChangedSinceResponse() {
        return new ListSelectionsChangedSinceResponse();
    }

    /**
     * Create an instance of {@link ListSelectionsChangedSinceResponse2 }
     * 
     */
    public ListSelectionsChangedSinceResponse2 createListSelectionsChangedSinceResponse2() {
        return new ListSelectionsChangedSinceResponse2();
    }

    /**
     * Create an instance of {@link ListBootstrapOrders }
     * 
     */
    public ListBootstrapOrders createListBootstrapOrders() {
        return new ListBootstrapOrders();
    }

    /**
     * Create an instance of {@link ListBootstrapOrdersRequest }
     * 
     */
    public ListBootstrapOrdersRequest createListBootstrapOrdersRequest() {
        return new ListBootstrapOrdersRequest();
    }

    /**
     * Create an instance of {@link ListBootstrapOrdersResponse }
     * 
     */
    public ListBootstrapOrdersResponse createListBootstrapOrdersResponse() {
        return new ListBootstrapOrdersResponse();
    }

    /**
     * Create an instance of {@link SuspendFromTrading }
     * 
     */
    public SuspendFromTrading createSuspendFromTrading() {
        return new SuspendFromTrading();
    }

    /**
     * Create an instance of {@link SuspendFromTradingRequest }
     * 
     */
    public SuspendFromTradingRequest createSuspendFromTradingRequest() {
        return new SuspendFromTradingRequest();
    }

    /**
     * Create an instance of {@link SuspendFromTradingResponse }
     * 
     */
    public SuspendFromTradingResponse createSuspendFromTradingResponse() {
        return new SuspendFromTradingResponse();
    }

    /**
     * Create an instance of {@link SuspendFromTradingResponse2 }
     * 
     */
    public SuspendFromTradingResponse2 createSuspendFromTradingResponse2() {
        return new SuspendFromTradingResponse2();
    }

    /**
     * Create an instance of {@link UnsuspendFromTrading }
     * 
     */
    public UnsuspendFromTrading createUnsuspendFromTrading() {
        return new UnsuspendFromTrading();
    }

    /**
     * Create an instance of {@link UnsuspendFromTradingRequest }
     * 
     */
    public UnsuspendFromTradingRequest createUnsuspendFromTradingRequest() {
        return new UnsuspendFromTradingRequest();
    }

    /**
     * Create an instance of {@link UnsuspendFromTradingResponse }
     * 
     */
    public UnsuspendFromTradingResponse createUnsuspendFromTradingResponse() {
        return new UnsuspendFromTradingResponse();
    }

    /**
     * Create an instance of {@link UnsuspendFromTradingResponse2 }
     * 
     */
    public UnsuspendFromTradingResponse2 createUnsuspendFromTradingResponse2() {
        return new UnsuspendFromTradingResponse2();
    }

    /**
     * Create an instance of {@link SuspendOrders }
     * 
     */
    public SuspendOrders createSuspendOrders() {
        return new SuspendOrders();
    }

    /**
     * Create an instance of {@link SuspendOrdersRequest }
     * 
     */
    public SuspendOrdersRequest createSuspendOrdersRequest() {
        return new SuspendOrdersRequest();
    }

    /**
     * Create an instance of {@link SuspendOrdersResponse }
     * 
     */
    public SuspendOrdersResponse createSuspendOrdersResponse() {
        return new SuspendOrdersResponse();
    }

    /**
     * Create an instance of {@link SuspendOrdersResponse2 }
     * 
     */
    public SuspendOrdersResponse2 createSuspendOrdersResponse2() {
        return new SuspendOrdersResponse2();
    }

    /**
     * Create an instance of {@link SuspendAllOrdersOnMarket }
     * 
     */
    public SuspendAllOrdersOnMarket createSuspendAllOrdersOnMarket() {
        return new SuspendAllOrdersOnMarket();
    }

    /**
     * Create an instance of {@link SuspendAllOrdersOnMarketRequest }
     * 
     */
    public SuspendAllOrdersOnMarketRequest createSuspendAllOrdersOnMarketRequest() {
        return new SuspendAllOrdersOnMarketRequest();
    }

    /**
     * Create an instance of {@link SuspendAllOrdersOnMarketResponse }
     * 
     */
    public SuspendAllOrdersOnMarketResponse createSuspendAllOrdersOnMarketResponse() {
        return new SuspendAllOrdersOnMarketResponse();
    }

    /**
     * Create an instance of {@link SuspendAllOrdersOnMarketResponse2 }
     * 
     */
    public SuspendAllOrdersOnMarketResponse2 createSuspendAllOrdersOnMarketResponse2() {
        return new SuspendAllOrdersOnMarketResponse2();
    }

    /**
     * Create an instance of {@link UnsuspendOrders }
     * 
     */
    public UnsuspendOrders createUnsuspendOrders() {
        return new UnsuspendOrders();
    }

    /**
     * Create an instance of {@link UnsuspendOrdersRequest }
     * 
     */
    public UnsuspendOrdersRequest createUnsuspendOrdersRequest() {
        return new UnsuspendOrdersRequest();
    }

    /**
     * Create an instance of {@link UnsuspendOrdersResponse }
     * 
     */
    public UnsuspendOrdersResponse createUnsuspendOrdersResponse() {
        return new UnsuspendOrdersResponse();
    }

    /**
     * Create an instance of {@link UnsuspendOrdersResponse2 }
     * 
     */
    public UnsuspendOrdersResponse2 createUnsuspendOrdersResponse2() {
        return new UnsuspendOrdersResponse2();
    }

    /**
     * Create an instance of {@link SuspendAllOrders }
     * 
     */
    public SuspendAllOrders createSuspendAllOrders() {
        return new SuspendAllOrders();
    }

    /**
     * Create an instance of {@link SuspendAllOrdersRequest }
     * 
     */
    public SuspendAllOrdersRequest createSuspendAllOrdersRequest() {
        return new SuspendAllOrdersRequest();
    }

    /**
     * Create an instance of {@link SuspendAllOrdersResponse }
     * 
     */
    public SuspendAllOrdersResponse createSuspendAllOrdersResponse() {
        return new SuspendAllOrdersResponse();
    }

    /**
     * Create an instance of {@link SuspendAllOrdersResponse2 }
     * 
     */
    public SuspendAllOrdersResponse2 createSuspendAllOrdersResponse2() {
        return new SuspendAllOrdersResponse2();
    }

    /**
     * Create an instance of {@link ListBlacklistInformation }
     * 
     */
    public ListBlacklistInformation createListBlacklistInformation() {
        return new ListBlacklistInformation();
    }

    /**
     * Create an instance of {@link ListBlacklistInformationRequest }
     * 
     */
    public ListBlacklistInformationRequest createListBlacklistInformationRequest() {
        return new ListBlacklistInformationRequest();
    }

    /**
     * Create an instance of {@link ListBlacklistInformationResponse }
     * 
     */
    public ListBlacklistInformationResponse createListBlacklistInformationResponse() {
        return new ListBlacklistInformationResponse();
    }

    /**
     * Create an instance of {@link ListBlacklistInformationResponse2 }
     * 
     */
    public ListBlacklistInformationResponse2 createListBlacklistInformationResponse2() {
        return new ListBlacklistInformationResponse2();
    }

    /**
     * Create an instance of {@link RegisterHeartbeat }
     * 
     */
    public RegisterHeartbeat createRegisterHeartbeat() {
        return new RegisterHeartbeat();
    }

    /**
     * Create an instance of {@link RegisterHeartbeatRequest }
     * 
     */
    public RegisterHeartbeatRequest createRegisterHeartbeatRequest() {
        return new RegisterHeartbeatRequest();
    }

    /**
     * Create an instance of {@link RegisterHeartbeatResponse }
     * 
     */
    public RegisterHeartbeatResponse createRegisterHeartbeatResponse() {
        return new RegisterHeartbeatResponse();
    }

    /**
     * Create an instance of {@link RegisterHeartbeatResponse2 }
     * 
     */
    public RegisterHeartbeatResponse2 createRegisterHeartbeatResponse2() {
        return new RegisterHeartbeatResponse2();
    }

    /**
     * Create an instance of {@link ChangeHeartbeatRegistration }
     * 
     */
    public ChangeHeartbeatRegistration createChangeHeartbeatRegistration() {
        return new ChangeHeartbeatRegistration();
    }

    /**
     * Create an instance of {@link ChangeHeartbeatRegistrationRequest }
     * 
     */
    public ChangeHeartbeatRegistrationRequest createChangeHeartbeatRegistrationRequest() {
        return new ChangeHeartbeatRegistrationRequest();
    }

    /**
     * Create an instance of {@link ChangeHeartbeatRegistrationResponse }
     * 
     */
    public ChangeHeartbeatRegistrationResponse createChangeHeartbeatRegistrationResponse() {
        return new ChangeHeartbeatRegistrationResponse();
    }

    /**
     * Create an instance of {@link ChangeHeartbeatRegistrationResponse2 }
     * 
     */
    public ChangeHeartbeatRegistrationResponse2 createChangeHeartbeatRegistrationResponse2() {
        return new ChangeHeartbeatRegistrationResponse2();
    }

    /**
     * Create an instance of {@link DeregisterHeartbeat }
     * 
     */
    public DeregisterHeartbeat createDeregisterHeartbeat() {
        return new DeregisterHeartbeat();
    }

    /**
     * Create an instance of {@link DeregisterHeartbeatRequest }
     * 
     */
    public DeregisterHeartbeatRequest createDeregisterHeartbeatRequest() {
        return new DeregisterHeartbeatRequest();
    }

    /**
     * Create an instance of {@link DeregisterHeartbeatResponse }
     * 
     */
    public DeregisterHeartbeatResponse createDeregisterHeartbeatResponse() {
        return new DeregisterHeartbeatResponse();
    }

    /**
     * Create an instance of {@link DeregisterHeartbeatResponse2 }
     * 
     */
    public DeregisterHeartbeatResponse2 createDeregisterHeartbeatResponse2() {
        return new DeregisterHeartbeatResponse2();
    }

    /**
     * Create an instance of {@link Pulse }
     * 
     */
    public Pulse createPulse() {
        return new Pulse();
    }

    /**
     * Create an instance of {@link PulseRequest }
     * 
     */
    public PulseRequest createPulseRequest() {
        return new PulseRequest();
    }

    /**
     * Create an instance of {@link PulseResponse }
     * 
     */
    public PulseResponse createPulseResponse() {
        return new PulseResponse();
    }

    /**
     * Create an instance of {@link PulseResponse2 }
     * 
     */
    public PulseResponse2 createPulseResponse2() {
        return new PulseResponse2();
    }

    /**
     * Create an instance of {@link GetOddsLadder }
     * 
     */
    public GetOddsLadder createGetOddsLadder() {
        return new GetOddsLadder();
    }

    /**
     * Create an instance of {@link GetOddsLadderRequest }
     * 
     */
    public GetOddsLadderRequest createGetOddsLadderRequest() {
        return new GetOddsLadderRequest();
    }

    /**
     * Create an instance of {@link GetOddsLadderResponse }
     * 
     */
    public GetOddsLadderResponse createGetOddsLadderResponse() {
        return new GetOddsLadderResponse();
    }

    /**
     * Create an instance of {@link GetOddsLadderResponse2 }
     * 
     */
    public GetOddsLadderResponse2 createGetOddsLadderResponse2() {
        return new GetOddsLadderResponse2();
    }

    /**
     * Create an instance of {@link GetCurrentSelectionSequenceNumber }
     * 
     */
    public GetCurrentSelectionSequenceNumber createGetCurrentSelectionSequenceNumber() {
        return new GetCurrentSelectionSequenceNumber();
    }

    /**
     * Create an instance of {@link GetCurrentSelectionSequenceNumberRequest }
     * 
     */
    public GetCurrentSelectionSequenceNumberRequest createGetCurrentSelectionSequenceNumberRequest() {
        return new GetCurrentSelectionSequenceNumberRequest();
    }

    /**
     * Create an instance of {@link GetCurrentSelectionSequenceNumberResponse }
     * 
     */
    public GetCurrentSelectionSequenceNumberResponse createGetCurrentSelectionSequenceNumberResponse() {
        return new GetCurrentSelectionSequenceNumberResponse();
    }

    /**
     * Create an instance of {@link GetCurrentSelectionSequenceNumberResponse2 }
     * 
     */
    public GetCurrentSelectionSequenceNumberResponse2 createGetCurrentSelectionSequenceNumberResponse2() {
        return new GetCurrentSelectionSequenceNumberResponse2();
    }

    /**
     * Create an instance of {@link ListSelectionTrades }
     * 
     */
    public ListSelectionTrades createListSelectionTrades() {
        return new ListSelectionTrades();
    }

    /**
     * Create an instance of {@link ListSelectionTradesRequest }
     * 
     */
    public ListSelectionTradesRequest createListSelectionTradesRequest() {
        return new ListSelectionTradesRequest();
    }

    /**
     * Create an instance of {@link ListSelectionTradesResponse }
     * 
     */
    public ListSelectionTradesResponse createListSelectionTradesResponse() {
        return new ListSelectionTradesResponse();
    }

    /**
     * Create an instance of {@link ListSelectionTradesResponse2 }
     * 
     */
    public ListSelectionTradesResponse2 createListSelectionTradesResponse2() {
        return new ListSelectionTradesResponse2();
    }

    /**
     * Create an instance of {@link GetSPEnabledMarketsInformation }
     * 
     */
    public GetSPEnabledMarketsInformation createGetSPEnabledMarketsInformation() {
        return new GetSPEnabledMarketsInformation();
    }

    /**
     * Create an instance of {@link GetSPEnabledMarketsInformationRequest }
     * 
     */
    public GetSPEnabledMarketsInformationRequest createGetSPEnabledMarketsInformationRequest() {
        return new GetSPEnabledMarketsInformationRequest();
    }

    /**
     * Create an instance of {@link GetSPEnabledMarketsInformationResponse }
     * 
     */
    public GetSPEnabledMarketsInformationResponse createGetSPEnabledMarketsInformationResponse() {
        return new GetSPEnabledMarketsInformationResponse();
    }

    /**
     * Create an instance of {@link ListTaggedValues }
     * 
     */
    public ListTaggedValues createListTaggedValues() {
        return new ListTaggedValues();
    }

    /**
     * Create an instance of {@link ListTaggedValuesRequest }
     * 
     */
    public ListTaggedValuesRequest createListTaggedValuesRequest() {
        return new ListTaggedValuesRequest();
    }

    /**
     * Create an instance of {@link ListTaggedValuesResponse }
     * 
     */
    public ListTaggedValuesResponse createListTaggedValuesResponse() {
        return new ListTaggedValuesResponse();
    }

    /**
     * Create an instance of {@link ListTaggedValuesResponse2 }
     * 
     */
    public ListTaggedValuesResponse2 createListTaggedValuesResponse2() {
        return new ListTaggedValuesResponse2();
    }

    /**
     * Create an instance of {@link ReturnStatus }
     * 
     */
    public ReturnStatus createReturnStatus() {
        return new ReturnStatus();
    }

    /**
     * Create an instance of {@link ListSelectionsChangedSinceResponseItem }
     * 
     */
    public ListSelectionsChangedSinceResponseItem createListSelectionsChangedSinceResponseItem() {
        return new ListSelectionsChangedSinceResponseItem();
    }

    /**
     * Create an instance of {@link SettlementInformationType }
     * 
     */
    public SettlementInformationType createSettlementInformationType() {
        return new SettlementInformationType();
    }

    /**
     * Create an instance of {@link Order }
     * 
     */
    public Order createOrder() {
        return new Order();
    }

    /**
     * Create an instance of {@link OrderCommissionInformationType }
     * 
     */
    public OrderCommissionInformationType createOrderCommissionInformationType() {
        return new OrderCommissionInformationType();
    }

    /**
     * Create an instance of {@link ArrayOfOrder }
     * 
     */
    public ArrayOfOrder createArrayOfOrder() {
        return new ArrayOfOrder();
    }

    /**
     * Create an instance of {@link SimpleOrderRequest }
     * 
     */
    public SimpleOrderRequest createSimpleOrderRequest() {
        return new SimpleOrderRequest();
    }

    /**
     * Create an instance of {@link Offer }
     * 
     */
    public Offer createOffer() {
        return new Offer();
    }

    /**
     * Create an instance of {@link PlaceOrdersNoReceiptResponseItem }
     * 
     */
    public PlaceOrdersNoReceiptResponseItem createPlaceOrdersNoReceiptResponseItem() {
        return new PlaceOrdersNoReceiptResponseItem();
    }

    /**
     * Create an instance of {@link CancelOrdersResponseItem }
     * 
     */
    public CancelOrdersResponseItem createCancelOrdersResponseItem() {
        return new CancelOrdersResponseItem();
    }

    /**
     * Create an instance of {@link PlaceOrdersWithReceiptResponseItem }
     * 
     */
    public PlaceOrdersWithReceiptResponseItem createPlaceOrdersWithReceiptResponseItem() {
        return new PlaceOrdersWithReceiptResponseItem();
    }

    /**
     * Create an instance of {@link PlaceOrdersWithReceiptRequestItem }
     * 
     */
    public PlaceOrdersWithReceiptRequestItem createPlaceOrdersWithReceiptRequestItem() {
        return new PlaceOrdersWithReceiptRequestItem();
    }

    /**
     * Create an instance of {@link ArrayOfInt }
     * 
     */
    public ArrayOfInt createArrayOfInt() {
        return new ArrayOfInt();
    }

    /**
     * Create an instance of {@link UpdateOrdersNoReceiptRequestItem }
     * 
     */
    public UpdateOrdersNoReceiptRequestItem createUpdateOrdersNoReceiptRequestItem() {
        return new UpdateOrdersNoReceiptRequestItem();
    }

    /**
     * Create an instance of {@link UpdateOrdersNoReceiptResponseItem }
     * 
     */
    public UpdateOrdersNoReceiptResponseItem createUpdateOrdersNoReceiptResponseItem() {
        return new UpdateOrdersNoReceiptResponseItem();
    }

    /**
     * Create an instance of {@link ListAccountPostingsResponseItem }
     * 
     */
    public ListAccountPostingsResponseItem createListAccountPostingsResponseItem() {
        return new ListAccountPostingsResponseItem();
    }

    /**
     * Create an instance of {@link ListAccountPostingsByIdResponseItem }
     * 
     */
    public ListAccountPostingsByIdResponseItem createListAccountPostingsByIdResponseItem() {
        return new ListAccountPostingsByIdResponseItem();
    }

    /**
     * Create an instance of {@link OrderSettlementInformationType }
     * 
     */
    public OrderSettlementInformationType createOrderSettlementInformationType() {
        return new OrderSettlementInformationType();
    }

    /**
     * Create an instance of {@link AuditLogItem }
     * 
     */
    public AuditLogItem createAuditLogItem() {
        return new AuditLogItem();
    }

    /**
     * Create an instance of {@link MatchedOrderInformationType }
     * 
     */
    public MatchedOrderInformationType createMatchedOrderInformationType() {
        return new MatchedOrderInformationType();
    }

    /**
     * Create an instance of {@link CommissionInformationType }
     * 
     */
    public CommissionInformationType createCommissionInformationType() {
        return new CommissionInformationType();
    }

    /**
     * Create an instance of {@link EventClassifierType }
     * 
     */
    public EventClassifierType createEventClassifierType() {
        return new EventClassifierType();
    }

    /**
     * Create an instance of {@link MarketType }
     * 
     */
    public MarketType createMarketType() {
        return new MarketType();
    }

    /**
     * Create an instance of {@link SelectionType }
     * 
     */
    public SelectionType createSelectionType() {
        return new SelectionType();
    }

    /**
     * Create an instance of {@link ListMarketWithdrawalHistoryResponseItem }
     * 
     */
    public ListMarketWithdrawalHistoryResponseItem createListMarketWithdrawalHistoryResponseItem() {
        return new ListMarketWithdrawalHistoryResponseItem();
    }

    /**
     * Create an instance of {@link SelectionTypeWithPrices }
     * 
     */
    public SelectionTypeWithPrices createSelectionTypeWithPrices() {
        return new SelectionTypeWithPrices();
    }

    /**
     * Create an instance of {@link MarketTypeWithPrices }
     * 
     */
    public MarketTypeWithPrices createMarketTypeWithPrices() {
        return new MarketTypeWithPrices();
    }

    /**
     * Create an instance of {@link PricesType }
     * 
     */
    public PricesType createPricesType() {
        return new PricesType();
    }

    /**
     * Create an instance of {@link CancelAllOrdersOnMarketResponseItem }
     * 
     */
    public CancelAllOrdersOnMarketResponseItem createCancelAllOrdersOnMarketResponseItem() {
        return new CancelAllOrdersOnMarketResponseItem();
    }

    /**
     * Create an instance of {@link CancelAllOrdersResponseItem }
     * 
     */
    public CancelAllOrdersResponseItem createCancelAllOrdersResponseItem() {
        return new CancelAllOrdersResponseItem();
    }

    /**
     * Create an instance of {@link SuspendOrdersResponseItem }
     * 
     */
    public SuspendOrdersResponseItem createSuspendOrdersResponseItem() {
        return new SuspendOrdersResponseItem();
    }

    /**
     * Create an instance of {@link ApiTimes }
     * 
     */
    public ApiTimes createApiTimes() {
        return new ApiTimes();
    }

    /**
     * Create an instance of {@link GetOddsLadderResponseItem }
     * 
     */
    public GetOddsLadderResponseItem createGetOddsLadderResponseItem() {
        return new GetOddsLadderResponseItem();
    }

    /**
     * Create an instance of {@link SelectionTradesRequestItem }
     * 
     */
    public SelectionTradesRequestItem createSelectionTradesRequestItem() {
        return new SelectionTradesRequestItem();
    }

    /**
     * Create an instance of {@link ListSelectionTradesResponseItem }
     * 
     */
    public ListSelectionTradesResponseItem createListSelectionTradesResponseItem() {
        return new ListSelectionTradesResponseItem();
    }

    /**
     * Create an instance of {@link TradeItemType }
     * 
     */
    public TradeItemType createTradeItemType() {
        return new TradeItemType();
    }

    /**
     * Create an instance of {@link ListTaggedValuesRequestEntity }
     * 
     */
    public ListTaggedValuesRequestEntity createListTaggedValuesRequestEntity() {
        return new ListTaggedValuesRequestEntity();
    }

    /**
     * Create an instance of {@link ListTaggedValuesResponseTaggedValue }
     * 
     */
    public ListTaggedValuesResponseTaggedValue createListTaggedValuesResponseTaggedValue() {
        return new ListTaggedValuesResponseTaggedValue();
    }

    /**
     * Create an instance of {@link ListTaggedValuesResponseInnerTaggedValue }
     * 
     */
    public ListTaggedValuesResponseInnerTaggedValue createListTaggedValuesResponseInnerTaggedValue() {
        return new ListTaggedValuesResponseInnerTaggedValue();
    }

    /**
     * Create an instance of {@link GetSPEnabledMarketsInformationResponse2 .SPEnabledEvent.MarketTypeIds }
     * 
     */
    public GetSPEnabledMarketsInformationResponse2 .SPEnabledEvent.MarketTypeIds createGetSPEnabledMarketsInformationResponse2SPEnabledEventMarketTypeIds() {
        return new GetSPEnabledMarketsInformationResponse2 .SPEnabledEvent.MarketTypeIds();
    }

    /**
     * Create an instance of {@link ListBootstrapOrdersResponse2 .Orders }
     * 
     */
    public ListBootstrapOrdersResponse2 .Orders createListBootstrapOrdersResponse2Orders() {
        return new ListBootstrapOrdersResponse2 .Orders();
    }

    /**
     * Create an instance of {@link GetOrderDetailsResponse2 .AuditLog }
     * 
     */
    public GetOrderDetailsResponse2 .AuditLog createGetOrderDetailsResponse2AuditLog() {
        return new GetOrderDetailsResponse2 .AuditLog();
    }

    /**
     * Create an instance of {@link UpdateOrdersNoReceiptResponse2 .Orders }
     * 
     */
    public UpdateOrdersNoReceiptResponse2 .Orders createUpdateOrdersNoReceiptResponse2Orders() {
        return new UpdateOrdersNoReceiptResponse2 .Orders();
    }

    /**
     * Create an instance of {@link UpdateOrdersNoReceiptRequest.Orders }
     * 
     */
    public UpdateOrdersNoReceiptRequest.Orders createUpdateOrdersNoReceiptRequestOrders() {
        return new UpdateOrdersNoReceiptRequest.Orders();
    }

    /**
     * Create an instance of {@link ListAccountPostingsByIdResponse2 .Orders }
     * 
     */
    public ListAccountPostingsByIdResponse2 .Orders createListAccountPostingsByIdResponse2Orders() {
        return new ListAccountPostingsByIdResponse2 .Orders();
    }

    /**
     * Create an instance of {@link ListAccountPostingsResponse2 .Orders }
     * 
     */
    public ListAccountPostingsResponse2 .Orders createListAccountPostingsResponse2Orders() {
        return new ListAccountPostingsResponse2 .Orders();
    }

    /**
     * Create an instance of {@link CancelAllOrdersResponse2 .CancelledOrdersHandles }
     * 
     */
    public CancelAllOrdersResponse2 .CancelledOrdersHandles createCancelAllOrdersResponse2CancelledOrdersHandles() {
        return new CancelAllOrdersResponse2 .CancelledOrdersHandles();
    }

    /**
     * Create an instance of {@link CancelAllOrdersResponse2 .Orders }
     * 
     */
    public CancelAllOrdersResponse2 .Orders createCancelAllOrdersResponse2Orders() {
        return new CancelAllOrdersResponse2 .Orders();
    }

    /**
     * Create an instance of {@link CancelOrdersResponse2 .CancelledOrdersHandles }
     * 
     */
    public CancelOrdersResponse2 .CancelledOrdersHandles createCancelOrdersResponse2CancelledOrdersHandles() {
        return new CancelOrdersResponse2 .CancelledOrdersHandles();
    }

    /**
     * Create an instance of {@link CancelOrdersResponse2 .Orders }
     * 
     */
    public CancelOrdersResponse2 .Orders createCancelOrdersResponse2Orders() {
        return new CancelOrdersResponse2 .Orders();
    }

    /**
     * Create an instance of {@link PlaceOrdersWithReceiptResponse2 .Orders }
     * 
     */
    public PlaceOrdersWithReceiptResponse2 .Orders createPlaceOrdersWithReceiptResponse2Orders() {
        return new PlaceOrdersWithReceiptResponse2 .Orders();
    }

    /**
     * Create an instance of {@link PlaceOrdersWithReceiptRequest.Orders }
     * 
     */
    public PlaceOrdersWithReceiptRequest.Orders createPlaceOrdersWithReceiptRequestOrders() {
        return new PlaceOrdersWithReceiptRequest.Orders();
    }

    /**
     * Create an instance of {@link PlaceOrdersNoReceiptResponse2 .OrderHandles }
     * 
     */
    public PlaceOrdersNoReceiptResponse2 .OrderHandles createPlaceOrdersNoReceiptResponse2OrderHandles() {
        return new PlaceOrdersNoReceiptResponse2 .OrderHandles();
    }

    /**
     * Create an instance of {@link PlaceOrdersNoReceiptResponse2 .Orders }
     * 
     */
    public PlaceOrdersNoReceiptResponse2 .Orders createPlaceOrdersNoReceiptResponse2Orders() {
        return new PlaceOrdersNoReceiptResponse2 .Orders();
    }

    /**
     * Create an instance of {@link PlaceOrdersNoReceiptRequest.Orders }
     * 
     */
    public PlaceOrdersNoReceiptRequest.Orders createPlaceOrdersNoReceiptRequestOrders() {
        return new PlaceOrdersNoReceiptRequest.Orders();
    }

    /**
     * Create an instance of {@link ListOrdersChangedSinceResponse2 .Orders }
     * 
     */
    public ListOrdersChangedSinceResponse2 .Orders createListOrdersChangedSinceResponse2Orders() {
        return new ListOrdersChangedSinceResponse2 .Orders();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExternalApiHeader }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ExternalApiHeader }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.GlobalBettingExchange.com/ExternalAPI/", name = "ExternalApiHeader")
    public JAXBElement<ExternalApiHeader> createExternalApiHeader(ExternalApiHeader value) {
        return new JAXBElement<ExternalApiHeader>(_ExternalApiHeader_QNAME, ExternalApiHeader.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PricesType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link PricesType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.GlobalBettingExchange.com/ExternalAPI/", name = "ForSidePrices", scope = SelectionTypeWithPrices.class)
    public JAXBElement<PricesType> createSelectionTypeWithPricesForSidePrices(PricesType value) {
        return new JAXBElement<PricesType>(_SelectionTypeWithPricesForSidePrices_QNAME, PricesType.class, SelectionTypeWithPrices.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PricesType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link PricesType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.GlobalBettingExchange.com/ExternalAPI/", name = "AgainstSidePrices", scope = SelectionTypeWithPrices.class)
    public JAXBElement<PricesType> createSelectionTypeWithPricesAgainstSidePrices(PricesType value) {
        return new JAXBElement<PricesType>(_SelectionTypeWithPricesAgainstSidePrices_QNAME, PricesType.class, SelectionTypeWithPrices.class, value);
    }

}
