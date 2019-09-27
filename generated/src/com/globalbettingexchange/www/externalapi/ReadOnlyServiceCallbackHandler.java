/**
 * ReadOnlyServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.7.9  Built on : Nov 16, 2018 (12:05:37 GMT)
 */
package com.globalbettingexchange.www.externalapi;


/**
 *  ReadOnlyServiceCallbackHandler Callback class, Users can extend this class and implement
 *  their own receiveResult and receiveError methods.
 */
public abstract class ReadOnlyServiceCallbackHandler {
    protected Object clientData;

    /**
     * User can pass in any object that needs to be accessed once the NonBlocking
     * Web service call is finished and appropriate method of this CallBack is called.
     * @param clientData Object mechanism by which the user can pass in user data
     * that will be avilable at the time this callback is called.
     */
    public ReadOnlyServiceCallbackHandler(Object clientData) {
        this.clientData = clientData;
    }

    /**
     * Please use this constructor if you don't want to set any clientData
     */
    public ReadOnlyServiceCallbackHandler() {
        this.clientData = null;
    }

    /**
     * Get the client data
     */
    public Object getClientData() {
        return clientData;
    }

    /**
     * auto generated Axis2 call back method for getCurrentSelectionSequenceNumber method
     * override this method for handling normal response from getCurrentSelectionSequenceNumber operation
     */
    public void receiveResultgetCurrentSelectionSequenceNumber(
        com.globalbettingexchange.www.externalapi.ReadOnlyServiceStub.GetCurrentSelectionSequenceNumberResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getCurrentSelectionSequenceNumber operation
     */
    public void receiveErrorgetCurrentSelectionSequenceNumber(
        java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getPrices method
     * override this method for handling normal response from getPrices operation
     */
    public void receiveResultgetPrices(
        com.globalbettingexchange.www.externalapi.ReadOnlyServiceStub.GetPricesResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getPrices operation
     */
    public void receiveErrorgetPrices(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getEventSubTreeNoSelections method
     * override this method for handling normal response from getEventSubTreeNoSelections operation
     */
    public void receiveResultgetEventSubTreeNoSelections(
        com.globalbettingexchange.www.externalapi.ReadOnlyServiceStub.GetEventSubTreeNoSelectionsResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getEventSubTreeNoSelections operation
     */
    public void receiveErrorgetEventSubTreeNoSelections(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getMarketInformation method
     * override this method for handling normal response from getMarketInformation operation
     */
    public void receiveResultgetMarketInformation(
        com.globalbettingexchange.www.externalapi.ReadOnlyServiceStub.GetMarketInformationResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getMarketInformation operation
     */
    public void receiveErrorgetMarketInformation(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for listMarketWithdrawalHistory method
     * override this method for handling normal response from listMarketWithdrawalHistory operation
     */
    public void receiveResultlistMarketWithdrawalHistory(
        com.globalbettingexchange.www.externalapi.ReadOnlyServiceStub.ListMarketWithdrawalHistoryResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from listMarketWithdrawalHistory operation
     */
    public void receiveErrorlistMarketWithdrawalHistory(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getOddsLadder method
     * override this method for handling normal response from getOddsLadder operation
     */
    public void receiveResultgetOddsLadder(
        com.globalbettingexchange.www.externalapi.ReadOnlyServiceStub.GetOddsLadderResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getOddsLadder operation
     */
    public void receiveErrorgetOddsLadder(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for listTaggedValues method
     * override this method for handling normal response from listTaggedValues operation
     */
    public void receiveResultlistTaggedValues(
        com.globalbettingexchange.www.externalapi.ReadOnlyServiceStub.ListTaggedValuesResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from listTaggedValues operation
     */
    public void receiveErrorlistTaggedValues(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getEventSubTreeWithSelections method
     * override this method for handling normal response from getEventSubTreeWithSelections operation
     */
    public void receiveResultgetEventSubTreeWithSelections(
        com.globalbettingexchange.www.externalapi.ReadOnlyServiceStub.GetEventSubTreeWithSelectionsResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getEventSubTreeWithSelections operation
     */
    public void receiveErrorgetEventSubTreeWithSelections(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for listSelectionTrades method
     * override this method for handling normal response from listSelectionTrades operation
     */
    public void receiveResultlistSelectionTrades(
        com.globalbettingexchange.www.externalapi.ReadOnlyServiceStub.ListSelectionTradesResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from listSelectionTrades operation
     */
    public void receiveErrorlistSelectionTrades(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for listTopLevelEvents method
     * override this method for handling normal response from listTopLevelEvents operation
     */
    public void receiveResultlistTopLevelEvents(
        com.globalbettingexchange.www.externalapi.ReadOnlyServiceStub.ListTopLevelEventsResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from listTopLevelEvents operation
     */
    public void receiveErrorlistTopLevelEvents(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getSPEnabledMarketsInformation method
     * override this method for handling normal response from getSPEnabledMarketsInformation operation
     */
    public void receiveResultgetSPEnabledMarketsInformation(
        com.globalbettingexchange.www.externalapi.ReadOnlyServiceStub.GetSPEnabledMarketsInformationResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getSPEnabledMarketsInformation operation
     */
    public void receiveErrorgetSPEnabledMarketsInformation(
        java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for listSelectionsChangedSince method
     * override this method for handling normal response from listSelectionsChangedSince operation
     */
    public void receiveResultlistSelectionsChangedSince(
        com.globalbettingexchange.www.externalapi.ReadOnlyServiceStub.ListSelectionsChangedSinceResponseE result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from listSelectionsChangedSince operation
     */
    public void receiveErrorlistSelectionsChangedSince(java.lang.Exception e) {
    }
}
