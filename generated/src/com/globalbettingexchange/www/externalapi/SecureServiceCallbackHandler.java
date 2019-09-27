/**
 * SecureServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.7.9  Built on : Nov 16, 2018 (12:05:37 GMT)
 */
package com.globalbettingexchange.www.externalapi;


/**
 *  SecureServiceCallbackHandler Callback class, Users can extend this class and implement
 *  their own receiveResult and receiveError methods.
 */
public abstract class SecureServiceCallbackHandler {
    protected Object clientData;

    /**
     * User can pass in any object that needs to be accessed once the NonBlocking
     * Web service call is finished and appropriate method of this CallBack is called.
     * @param clientData Object mechanism by which the user can pass in user data
     * that will be avilable at the time this callback is called.
     */
    public SecureServiceCallbackHandler(Object clientData) {
        this.clientData = clientData;
    }

    /**
     * Please use this constructor if you don't want to set any clientData
     */
    public SecureServiceCallbackHandler() {
        this.clientData = null;
    }

    /**
     * Get the client data
     */
    public Object getClientData() {
        return clientData;
    }

    /**
     * auto generated Axis2 call back method for cancelOrders method
     * override this method for handling normal response from cancelOrders operation
     */
    public void receiveResultcancelOrders(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.CancelOrdersResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from cancelOrders operation
     */
    public void receiveErrorcancelOrders(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getAccountBalances method
     * override this method for handling normal response from getAccountBalances operation
     */
    public void receiveResultgetAccountBalances(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.GetAccountBalancesResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getAccountBalances operation
     */
    public void receiveErrorgetAccountBalances(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for listAccountPostings method
     * override this method for handling normal response from listAccountPostings operation
     */
    public void receiveResultlistAccountPostings(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.ListAccountPostingsResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from listAccountPostings operation
     */
    public void receiveErrorlistAccountPostings(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for listBootstrapOrders method
     * override this method for handling normal response from listBootstrapOrders operation
     */
    public void receiveResultlistBootstrapOrders(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.ListBootstrapOrdersResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from listBootstrapOrders operation
     */
    public void receiveErrorlistBootstrapOrders(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for suspendAllOrders method
     * override this method for handling normal response from suspendAllOrders operation
     */
    public void receiveResultsuspendAllOrders(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.SuspendAllOrdersResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from suspendAllOrders operation
     */
    public void receiveErrorsuspendAllOrders(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for listBlacklistInformation method
     * override this method for handling normal response from listBlacklistInformation operation
     */
    public void receiveResultlistBlacklistInformation(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.ListBlacklistInformationResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from listBlacklistInformation operation
     */
    public void receiveErrorlistBlacklistInformation(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for placeOrdersNoReceipt method
     * override this method for handling normal response from placeOrdersNoReceipt operation
     */
    public void receiveResultplaceOrdersNoReceipt(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.PlaceOrdersNoReceiptResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from placeOrdersNoReceipt operation
     */
    public void receiveErrorplaceOrdersNoReceipt(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for cancelAllOrdersOnMarket method
     * override this method for handling normal response from cancelAllOrdersOnMarket operation
     */
    public void receiveResultcancelAllOrdersOnMarket(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.CancelAllOrdersOnMarketResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from cancelAllOrdersOnMarket operation
     */
    public void receiveErrorcancelAllOrdersOnMarket(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for unsuspendOrders method
     * override this method for handling normal response from unsuspendOrders operation
     */
    public void receiveResultunsuspendOrders(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.UnsuspendOrdersResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from unsuspendOrders operation
     */
    public void receiveErrorunsuspendOrders(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for listOrdersChangedSince method
     * override this method for handling normal response from listOrdersChangedSince operation
     */
    public void receiveResultlistOrdersChangedSince(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.ListOrdersChangedSinceResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from listOrdersChangedSince operation
     */
    public void receiveErrorlistOrdersChangedSince(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for suspendAllOrdersOnMarket method
     * override this method for handling normal response from suspendAllOrdersOnMarket operation
     */
    public void receiveResultsuspendAllOrdersOnMarket(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.SuspendAllOrdersOnMarketResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from suspendAllOrdersOnMarket operation
     */
    public void receiveErrorsuspendAllOrdersOnMarket(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for deregisterHeartbeat method
     * override this method for handling normal response from deregisterHeartbeat operation
     */
    public void receiveResultderegisterHeartbeat(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.DeregisterHeartbeatResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from deregisterHeartbeat operation
     */
    public void receiveErrorderegisterHeartbeat(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for placeOrdersWithReceipt method
     * override this method for handling normal response from placeOrdersWithReceipt operation
     */
    public void receiveResultplaceOrdersWithReceipt(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.PlaceOrdersWithReceiptResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from placeOrdersWithReceipt operation
     */
    public void receiveErrorplaceOrdersWithReceipt(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for pulse method
     * override this method for handling normal response from pulse operation
     */
    public void receiveResultpulse(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.PulseResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from pulse operation
     */
    public void receiveErrorpulse(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for cancelAllOrders method
     * override this method for handling normal response from cancelAllOrders operation
     */
    public void receiveResultcancelAllOrders(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.CancelAllOrdersResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from cancelAllOrders operation
     */
    public void receiveErrorcancelAllOrders(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for suspendOrders method
     * override this method for handling normal response from suspendOrders operation
     */
    public void receiveResultsuspendOrders(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.SuspendOrdersResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from suspendOrders operation
     */
    public void receiveErrorsuspendOrders(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for registerHeartbeat method
     * override this method for handling normal response from registerHeartbeat operation
     */
    public void receiveResultregisterHeartbeat(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.RegisterHeartbeatResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from registerHeartbeat operation
     */
    public void receiveErrorregisterHeartbeat(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for suspendFromTrading method
     * override this method for handling normal response from suspendFromTrading operation
     */
    public void receiveResultsuspendFromTrading(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.SuspendFromTradingResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from suspendFromTrading operation
     */
    public void receiveErrorsuspendFromTrading(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for updateOrdersNoReceipt method
     * override this method for handling normal response from updateOrdersNoReceipt operation
     */
    public void receiveResultupdateOrdersNoReceipt(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.UpdateOrdersNoReceiptResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from updateOrdersNoReceipt operation
     */
    public void receiveErrorupdateOrdersNoReceipt(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for changeHeartbeatRegistration method
     * override this method for handling normal response from changeHeartbeatRegistration operation
     */
    public void receiveResultchangeHeartbeatRegistration(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.ChangeHeartbeatRegistrationResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from changeHeartbeatRegistration operation
     */
    public void receiveErrorchangeHeartbeatRegistration(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for listAccountPostingsById method
     * override this method for handling normal response from listAccountPostingsById operation
     */
    public void receiveResultlistAccountPostingsById(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.ListAccountPostingsByIdResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from listAccountPostingsById operation
     */
    public void receiveErrorlistAccountPostingsById(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getOrderDetails method
     * override this method for handling normal response from getOrderDetails operation
     */
    public void receiveResultgetOrderDetails(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.GetOrderDetailsResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getOrderDetails operation
     */
    public void receiveErrorgetOrderDetails(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for changePassword method
     * override this method for handling normal response from changePassword operation
     */
    public void receiveResultchangePassword(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.ChangePasswordResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from changePassword operation
     */
    public void receiveErrorchangePassword(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for unsuspendFromTrading method
     * override this method for handling normal response from unsuspendFromTrading operation
     */
    public void receiveResultunsuspendFromTrading(
        com.globalbettingexchange.www.externalapi.SecureServiceStub.UnsuspendFromTradingResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from unsuspendFromTrading operation
     */
    public void receiveErrorunsuspendFromTrading(java.lang.Exception e) {
    }
}
