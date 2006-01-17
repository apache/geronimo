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

import javax.naming.*;

import org.apache.geronimo.samples.daytrader.direct.*;
import org.apache.geronimo.samples.daytrader.ejb.*;
import org.apache.geronimo.samples.daytrader.util.*;

import java.util.Collection;
import java.math.BigDecimal;
// import com.ibm.websphere.cache.*;

/**
 * The TradeAction class provides the generic client side access to each of the
 * Trade brokerage user operations. These include login, logout, buy, sell,
 * getQuote, etc. The TradeAction class does not handle user interface
 * processing and should be used by a class that is UI specific. For example,
 * {@link trade_client.TradeServletAction}manages a web interface to Trade,
 * making calls to TradeAction methods to actually performance each operation.
 *  
 */
public class TradeAction implements TradeServices {
	private TradeServices trade = null;
	private static TradeHome tradeHome = null;
	

	public TradeAction() {
		if (Log.doTrace())
			Log.trace("TradeAction:TradeAction()");
		createTrade();
	}

	public TradeAction(TradeServices trade) {
		if (Log.doActionTrace())
			Log.trace("TradeAction:TradeAction(trade)");
		this.trade = trade;
	}

	private void createTrade() {
		if (TradeConfig.runTimeMode == TradeConfig.EJB) {
			try {
				if (tradeHome == null) {
					InitialContext ic = new InitialContext();
					try {
						tradeHome = (TradeHome) (javax.rmi.PortableRemoteObject.narrow(ic.lookup("java:comp/env/ejb/Trade"), TradeHome.class));
					}
					catch (Exception e) {
						Log.log("TradeAction:createTrade lookup of java:comp/env/ejb/Trade failed. Reverting to JNDI lookup of Trade");
						tradeHome = (TradeHome) (javax.rmi.PortableRemoteObject.narrow(ic.lookup("Trade"), TradeHome.class));
					}
				}
				trade = tradeHome.create();
			}
			catch (Exception e) {
				Log.error("TradeAction:TradeAction() Creation of Trade EJB failed\n" + e);
				e.printStackTrace();
			}
		}
		else if (TradeConfig.runTimeMode == TradeConfig.DIRECT) {
			try {
				trade = new TradeDirect();
			}
			catch (Exception e) {
				Log.error("TradeAction:TradeAction() Creation of Trade Direct failed\n" + e);
			}
		}
	}

	/**
	 * Compute and return a snapshot of the current market conditions This
	 * includes the TSIA - an index of the price of the top 100 Trade stock
	 * quotes The openTSIA ( the index at the open) The volume of shares traded,
	 * Top Stocks gain and loss
	 * 
	 * @return A snapshot of the current market summary
	 */
	public MarketSummaryDataBean getMarketSummary() throws Exception {
		if (Log.doActionTrace()) {
			Log.trace("TradeAction:getMarketSummary()");
		}
		MarketSummaryDataBean marketSummaryData = null;
		marketSummaryData = trade.getMarketSummary();
		return marketSummaryData;
	}

	/**
	 * Purchase a stock and create a new holding for the given user. Given a
	 * stock symbol and quantity to purchase, retrieve the current quote price,
	 * debit the user's account balance, and add holdings to user's portfolio.
	 * 
	 * @param userID
	 *            the customer requesting the stock purchase
	 * @param symbol
	 *            the symbol of the stock being purchased
	 * @param quantity
	 *            the quantity of shares to purchase
	 * @return OrderDataBean providing the status of the newly created buy order
	 */
	public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) throws Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:buy", userID, symbol, new Double(quantity), new Integer(orderProcessingMode));
		OrderDataBean orderData = null;
		orderData = trade.buy(userID, symbol, quantity, orderProcessingMode);
		//after the purchase or sell of a stock, update the stocks volume and
		// price
		updateQuotePriceVolume(symbol, TradeConfig.getRandomPriceChangeFactor(), quantity);
		return orderData;
	}

	/**
	 * Sell(SOAP 2.2 Wrapper converting int to Integer) a stock holding and
	 * removed the holding for the given user. Given a Holding, retrieve current
	 * quote, credit user's account, and reduce holdings in user's portfolio.
	 * 
	 * @param userID
	 *            the customer requesting the sell
	 * @param holdingID
	 *            the users holding to be sold
	 * @return OrderDataBean providing the status of the newly created sell
	 *         order
	 */
	public OrderDataBean sell(String userID, int holdingID, int orderProcessingMode) throws Exception {
		return sell(userID, new Integer(holdingID), orderProcessingMode);
	}

	/**
	 * Sell a stock holding and removed the holding for the given user. Given a
	 * Holding, retrieve current quote, credit user's account, and reduce
	 * holdings in user's portfolio.
	 * 
	 * @param userID
	 *            the customer requesting the sell
	 * @param holdingID
	 *            the users holding to be sold
	 * @return OrderDataBean providing the status of the newly created sell
	 *         order
	 */
	public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) throws Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:sell", userID, holdingID, new Integer(orderProcessingMode));
		OrderDataBean orderData = null;
			orderData = trade.sell(userID, holdingID, orderProcessingMode);
		if (!(orderData.getOrderStatus().equalsIgnoreCase("cancelled")))
			//after the purchase or sell of a stock, update the stocks volume
			// and price
			updateQuotePriceVolume(orderData.getSymbol(), TradeConfig.getRandomPriceChangeFactor(), orderData.getQuantity());
		return orderData;
	}

	/**
	 * Queue the Order identified by orderID to be processed
	 * 
	 * Orders are submitted through JMS to a Trading Broker and completed
	 * asynchronously. This method queues the order for processing
	 * 
	 * The boolean twoPhase specifies to the server implementation whether or
	 * not the method is to participate in a global transaction
	 * 
	 * @param orderID
	 *            the Order being queued for processing
	 * @return OrderDataBean providing the status of the completed order
	 */
	public void queueOrder(Integer orderID, boolean twoPhase) {
		throw new UnsupportedOperationException("TradeAction: queueOrder method not supported");
	}

	/**
	 * Complete the Order identefied by orderID Orders are submitted through JMS
	 * to a Trading agent and completed asynchronously. This method completes
	 * the order For a buy, the stock is purchased creating a holding and the
	 * users account is debited For a sell, the stock holding is removed and the
	 * users account is credited with the proceeds
	 * 
	 * The boolean twoPhase specifies to the server implementation whether or
	 * not the method is to participate in a global transaction
	 * 
	 * @param orderID
	 *            the Order to complete
	 * @return OrderDataBean providing the status of the completed order
	 */
	public OrderDataBean completeOrder(Integer orderID, boolean twoPhase) {
		throw new UnsupportedOperationException("TradeAction: completeOrder method not supported");
	}

	/**
	 * Cancel the Order identified by orderID
	 * 
	 * Orders are submitted through JMS to a Trading Broker and completed
	 * asynchronously. This method queues the order for processing
	 * 
	 * The boolean twoPhase specifies to the server implementation whether or
	 * not the method is to participate in a global transaction
	 * 
	 * @param orderID
	 *            the Order being queued for processing
	 * @return OrderDataBean providing the status of the completed order
	 */
	public void cancelOrder(Integer orderID, boolean twoPhase) {
		throw new UnsupportedOperationException("TradeAction: cancelOrder method not supported");
	}

	public void orderCompleted(String userID, Integer orderID) throws Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:orderCompleted", userID, orderID);
		if (Log.doTrace())
			Log.trace("OrderCompleted", userID, orderID);
		return;
	}

	/**
	 * Get the collection of all orders for a given account
	 * 
	 * @param userID
	 *            the customer account to retrieve orders for
	 * @return Collection OrderDataBeans providing detailed order information
	 */
	public Collection getOrders(String userID) throws Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:getOrders", userID);
		Collection orderDataBeans = null;
		orderDataBeans = trade.getOrders(userID);
		return orderDataBeans;
	}

	/**
	 * Get the collection of completed orders for a given account that need to
	 * be alerted to the user
	 * 
	 * @param userID
	 *            the customer account to retrieve orders for
	 * @return Collection OrderDataBeans providing detailed order information
	 */
	public Collection getClosedOrders(String userID) throws Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:getClosedOrders", userID);
		Collection orderDataBeans = null;
		orderDataBeans = trade.getClosedOrders(userID);
		return orderDataBeans;
	}

	/**
	 * Given a market symbol, price, and details, create and return a new
	 * {@link QuoteDataBean}
	 * 
	 * @param symbol
	 *            the symbol of the stock
	 * @param price
	 *            the current stock price
	 * @param details
	 *            a short description of the stock or company
	 * @return a new QuoteDataBean or null if Quote could not be created
	 */
	public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) throws Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:createQuote", symbol, companyName, price);
		QuoteDataBean quoteData = null;
		quoteData = trade.createQuote(symbol, companyName, price);
		return quoteData;
	}

	/**
	 * Return a collection of {@link QuoteDataBean}describing all current
	 * quotes
	 * 
	 * @return the collection of QuoteDataBean
	 */
	public Collection getAllQuotes() throws Exception {
		if (Log.doActionTrace()) {
			Log.trace("TradeAction:getAllQuotes");
		}
		Collection quotes = null;
		quotes = trade.getAllQuotes();
		return quotes;
	}

	/**
	 * Return a {@link QuoteDataBean}describing a current quote for the given
	 * stock symbol
	 * 
	 * @param symbol
	 *            the stock symbol to retrieve the current Quote
	 * @return the QuoteDataBean
	 */
	public QuoteDataBean getQuote(String symbol) throws Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:getQuote", symbol);
		if ((symbol == null) || (symbol.length() == 0) || (symbol.length() > 10)) {
			if (Log.doActionTrace()) {
				Log.trace("TradeAction:getQuote   ---  primitive workload");
			}
			return new QuoteDataBean("Invalid symbol", "", 0.0, FinancialUtils.ZERO, FinancialUtils.ZERO, FinancialUtils.ZERO, FinancialUtils.ZERO, 0.0);
		}
		QuoteDataBean quoteData = null;
		quoteData = trade.getQuote(symbol);
		return quoteData;
	}

	/**
	 * Update the stock quote price for the specified stock symbol
	 * 
	 * @param symbol
	 *            for stock quote to update
	 * @param price
	 *            the updated quote price
	 * @return the QuoteDataBean describing the stock
	 */
	/* avoid data collision with synch */
	public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal changeFactor, double sharesTraded) throws Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:updateQuotePriceVolume", symbol, changeFactor, new Double(sharesTraded));
		QuoteDataBean quoteData = null;
		try {
			quoteData = trade.updateQuotePriceVolume(symbol, changeFactor, sharesTraded);
		}
		catch (Exception e) {
			Log.error("TradeAction:updateQuotePrice -- ", e);
		}
		return quoteData;
	}

	/**
	 * Return the portfolio of stock holdings for the specified customer as a
	 * collection of HoldingDataBeans
	 * 
	 * @param userID
	 *            the customer requesting the portfolio
	 * @return Collection of the users portfolio of stock holdings
	 */
	public Collection getHoldings(String userID) throws Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:getHoldings", userID);
		Collection holdingDataBeans = null;
		holdingDataBeans = trade.getHoldings(userID);
		return holdingDataBeans;
	}

	/**
	 * Return a specific user stock holding identifed by the holdingID
	 * 
	 * @param holdingID
	 *            the holdingID to return
	 * @return a HoldingDataBean describing the holding
	 */
	public HoldingDataBean getHolding(Integer holdingID) throws Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:getHolding", holdingID);
		HoldingDataBean holdingData = null;
		holdingData = trade.getHolding(holdingID);
		return holdingData;
	}

	/**
	 * Return an AccountDataBean object for userID describing the account
	 * 
	 * @param userID
	 *            the account userID to lookup
	 * @return User account data in AccountDataBean
	 */
	public AccountDataBean getAccountData(String userID) throws javax.ejb.FinderException, Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:getAccountData", userID);
		AccountDataBean accountData = null;
		accountData = trade.getAccountData(userID);
		return accountData;
	}

	/**
	 * Return an AccountProfileDataBean for userID providing the users profile
	 * 
	 * @param userID
	 *            the account userID to lookup
	 * @param User
	 *            account profile data in AccountProfileDataBean
	 */
	public AccountProfileDataBean getAccountProfileData(String userID) throws Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:getAccountProfileData", userID);
		AccountProfileDataBean accountProfileData = null;
		accountProfileData = trade.getAccountProfileData(userID);
		return accountProfileData;
	}

	/**
	 * Update userID's account profile information using the provided
	 * AccountProfileDataBean object
	 * 
	 * @param userID
	 *            the account userID to lookup
	 * @param User
	 *            account profile data in AccountProfileDataBean
	 */
	public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean accountProfileData) throws Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:updateAccountProfile", accountProfileData);
		accountProfileData = trade.updateAccountProfile(accountProfileData);
		return accountProfileData;
	}

	/**
	 * Attempt to authenticate and login a user with the given password
	 * 
	 * @param userID
	 *            the customer to login
	 * @param password
	 *            the password entered by the customer for authentication
	 * @return User account data in AccountDataBean
	 */
	public AccountDataBean login(String userID, String password) throws Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:login", userID, password);
		AccountDataBean accountData = null;
		accountData = trade.login(userID, password);
		return accountData;
	}

	/**
	 * Logout the given user
	 * 
	 * @param userID
	 *            the customer to logout
	 * @return the login status
	 */
	public void logout(String userID) throws Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:logout", userID);
		trade.logout(userID);
		return;
	}

	/**
	 * Register a new Trade customer. Create a new user profile, user registry
	 * entry, account with initial balance, and empty portfolio.
	 * 
	 * @param userID
	 *            the new customer to register
	 * @param password
	 *            the customers password
	 * @param fullname
	 *            the customers fullname
	 * @param address
	 *            the customers street address
	 * @param email
	 *            the customers email address
	 * @param creditcard
	 *            the customers creditcard number
	 * @param initialBalance
	 *            the amount to charge to the customers credit to open the
	 *            account and set the initial balance
	 * @return the userID if successful, null otherwise
	 */
	public AccountDataBean register(String userID, String password, String fullname, String address, String email, String creditCard, BigDecimal openBalance) throws Exception {
		if (Log.doActionTrace())
			Log.trace("TradeAction:register", userID, password, fullname, address, email, creditCard, openBalance);
		AccountDataBean accountData = null;
		accountData = trade.register(userID, password, fullname, address, email, creditCard, openBalance);
		return accountData;
	}

	public AccountDataBean register(String userID, String password, String fullname, String address, String email, String creditCard, String openBalanceString) throws Exception {
		BigDecimal openBalance = new BigDecimal(openBalanceString);
		return register(userID, password, fullname, address, email, creditCard, openBalance);
	}

	/**
	 * Reset the TradeData by - removing all newly registered users by scenario
	 * servlet (i.e. users with userID's beginning with "ru:") * - removing all
	 * buy/sell order pairs - setting logoutCount = loginCount
	 * 
	 * return statistics for this benchmark run
	 */
	public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception {
		RunStatsDataBean runStatsData = null;
		runStatsData = trade.resetTrade(deleteAll);
		return runStatsData;
	}
}