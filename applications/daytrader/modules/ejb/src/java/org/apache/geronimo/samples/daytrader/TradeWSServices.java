/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable 
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.samples.daytrader;

public interface TradeWSServices extends java.rmi.Remote {
    public org.apache.geronimo.samples.daytrader.MarketSummaryDataBeanWS getMarketSummary() throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.OrderDataBean buy(java.lang.String userID, java.lang.String symbol, double quantity, int orderProcessingMode) throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.OrderDataBean sell(java.lang.String userID, java.lang.Integer holdingID, int orderProcessingMode) throws java.rmi.RemoteException;
    public void queueOrder(java.lang.Integer orderID, boolean twoPhase) throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.OrderDataBean completeOrder(java.lang.Integer orderID, boolean twoPhase) throws java.rmi.RemoteException;
    public void cancelOrder(java.lang.Integer orderID, boolean twoPhase) throws java.rmi.RemoteException;
    public void orderCompleted(java.lang.String userID, java.lang.Integer orderID) throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.OrderDataBean[] getOrders(java.lang.String userID) throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.OrderDataBean[] getClosedOrders(java.lang.String userID) throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.QuoteDataBean createQuote(java.lang.String symbol, java.lang.String companyName, java.math.BigDecimal price) throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.QuoteDataBean getQuote(java.lang.String symbol) throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.QuoteDataBean[] getAllQuotes() throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.QuoteDataBean updateQuotePriceVolume(java.lang.String symbol, java.math.BigDecimal newPrice, double sharesTraded) throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.HoldingDataBean[] getHoldings(java.lang.String userID) throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.HoldingDataBean getHolding(java.lang.Integer holdingID) throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.AccountDataBean getAccountData(java.lang.String userID) throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.AccountProfileDataBean getAccountProfileData(java.lang.String userID) throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.AccountProfileDataBean updateAccountProfile(org.apache.geronimo.samples.daytrader.AccountProfileDataBean profileData) throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.AccountDataBean login(java.lang.String userID, java.lang.String password) throws java.rmi.RemoteException;
    public void logout(java.lang.String userID) throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.AccountDataBean register(java.lang.String userID, java.lang.String password, java.lang.String fullname, java.lang.String address, java.lang.String email, java.lang.String creditcard, java.math.BigDecimal openBalance) throws java.rmi.RemoteException;
    public org.apache.geronimo.samples.daytrader.RunStatsDataBean resetTrade(boolean deleteAll) throws java.rmi.RemoteException;
}
