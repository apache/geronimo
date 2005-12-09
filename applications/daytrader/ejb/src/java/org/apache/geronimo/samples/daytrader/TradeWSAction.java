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

import java.math.*;
import java.rmi.RemoteException;

/**
 * @author aspyker
 *
 * This is a TradeAction wrapper to handle web service handling
 * of collections.  Instead this class uses typed arrays.
 */
public class TradeWSAction {
	TradeAction trade;
	
	public TradeWSAction() {
		trade = new TradeAction();
	}

	public MarketSummaryDataBeanWS getMarketSummary() throws Exception, RemoteException {
		org.apache.geronimo.samples.daytrader.MarketSummaryDataBean marketSummary = trade.getMarketSummary();
		return MarketSummaryDataBeanWS.convertBean(marketSummary);
	}
	
	public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) throws Exception, RemoteException {
		return trade.buy(userID, symbol, quantity, orderProcessingMode);
	}
	
	public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) throws Exception, RemoteException {
		return trade.sell(userID, holdingID, orderProcessingMode);
	}

	public void queueOrder(Integer orderID, boolean twoPhase) throws Exception, RemoteException {
		trade.queueOrder(orderID, twoPhase);
	}
	
	public OrderDataBean completeOrder(Integer orderID, boolean twoPhase) throws Exception, RemoteException {
		return trade.completeOrder(orderID, twoPhase);
	}

	public void cancelOrder(Integer orderID, boolean twoPhase) throws Exception, RemoteException {
		trade.cancelOrder(orderID, twoPhase);
	}
	
	public void orderCompleted(String userID, Integer orderID) throws Exception, RemoteException {
		trade.orderCompleted(userID, orderID);
	}
	
	public OrderDataBean[] getOrders(String userID) throws Exception, RemoteException {
		return (OrderDataBean[])((trade.getOrders(userID)).toArray(new OrderDataBean[0]));
	}
	
	public OrderDataBean[] getClosedOrders(String userID) throws Exception, RemoteException {
		return (OrderDataBean[])((trade.getClosedOrders(userID)).toArray(new OrderDataBean[0]));
	}
	
	public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) throws Exception, RemoteException {
		return trade.createQuote(symbol, companyName, price);
	}
	
	public QuoteDataBean getQuote(String symbol) throws Exception, RemoteException {
		return trade.getQuote(symbol);
	}
	
	public QuoteDataBean[] getAllQuotes() throws Exception, RemoteException {
		return (QuoteDataBean[])((trade.getAllQuotes()).toArray(new QuoteDataBean[0]));
	}
	
	public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal newPrice, double sharesTraded) throws Exception, RemoteException {
		return trade.updateQuotePriceVolume(symbol, newPrice, sharesTraded);
	}
	
	public HoldingDataBean[] getHoldings(String userID) throws Exception, RemoteException {
		return (HoldingDataBean[])((trade.getHoldings(userID)).toArray(new HoldingDataBean[0]));
	}
	
	public HoldingDataBean getHolding(Integer holdingID) throws Exception, RemoteException {
		return trade.getHolding(holdingID);
	}
	
	public AccountDataBean getAccountData(String userID) throws Exception, RemoteException {
		return trade.getAccountData(userID);
	}
	
	public AccountProfileDataBean getAccountProfileData(String userID) throws Exception, RemoteException {
		return trade.getAccountProfileData(userID);
	}
	
	public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws Exception, RemoteException {
		return trade.updateAccountProfile(profileData);
	}
	
	public AccountDataBean login(String userID, String password) throws Exception, RemoteException {
		return trade.login(userID, password);
	}
	
	public void logout(String userID) throws Exception, RemoteException {
		trade.logout(userID);
	}
	
	public AccountDataBean register(String userID, String password, String fullname, String address, String email, String creditcard, BigDecimal openBalance) throws Exception, RemoteException {
		return trade.register(userID, password, fullname, address, email, creditcard, openBalance);
	}
	
	public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception, RemoteException {
		return trade.resetTrade(deleteAll);
	}
}
