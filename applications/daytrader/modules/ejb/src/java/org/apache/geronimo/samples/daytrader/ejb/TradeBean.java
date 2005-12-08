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

package org.apache.geronimo.samples.daytrader.ejb;

import javax.ejb.*;
import javax.jms.*;
import javax.naming.*;

import org.apache.geronimo.samples.daytrader.util.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.geronimo.samples.daytrader.*;

public class TradeBean implements SessionBean {

	private SessionContext context = null;
	private LocalAccountHome accountHome = null;
	private LocalAccountProfileHome profileHome = null;
	private LocalHoldingHome holdingHome = null;
	private LocalQuoteHome quoteHome = null;
	private LocalOrderHome orderHome = null;
	private LocalKeySequenceHome keySequenceHome = null;
	private LocalKeySequence keySequence;	
	
	private ConnectionFactory qConnFactory = null;
	private Queue queue = null; 
	private ConnectionFactory tConnFactory = null;
	private Topic streamerTopic = null; 

	//Boolean to signify if the Order By clause is supported by the app server.
	// This can be set to false by an env. variable
	private boolean orderBySQLSupported = true;  
	private boolean publishQuotePriceChange = true;  
	private boolean updateQuotePrices = true;  
   
    private void queueOrderInternal(Integer orderID, boolean twoPhase)
    	throws javax.jms.JMSException
    {
		if (Log.doTrace() ) Log.trace("TradeBean:queueOrderInternal", orderID);

		Connection conn = null;
		Session sess = null;
		try
		{
			conn = qConnFactory.createConnection();                        
			sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer msgProducer = sess.createProducer(queue);
			TextMessage   message = sess.createTextMessage();

			message.setStringProperty("command", "neworder");
			message.setIntProperty("orderID", orderID.intValue());
			message.setBooleanProperty("twoPhase", twoPhase);
			message.setText("neworder: orderID="+orderID + " runtimeMode=EJB twoPhase="+twoPhase);
			message.setLongProperty("publishTime", System.currentTimeMillis());						

			if (Log.doTrace()) Log.trace("TradeBean:queueOrder Sending message: " + message.getText());
			msgProducer.send(message);
		}
		catch (javax.jms.JMSException e)
		{
			throw e; // pass the exception back
		}
		
		finally {
			if (conn != null )
				conn.close();
			if (sess != null)
				sess.close();
		}
	}
	
   /** 
	 * @see TradeServices#queueOrder(Integer)
	 */

	public void queueOrder(Integer orderID, boolean twoPhase)
	throws Exception
	{
		if (Log.doTrace() ) Log.trace("TradeBean:queueOrder", orderID, new Boolean(twoPhase));
		if (twoPhase)
			queueOrderInternal(orderID, true);
		else
		{
			// invoke the queueOrderOnePhase method -- which requires a new transaction
			// the queueOrder will run in it's own transaction thus not requiring a 
			// 2-phase commit
			((Trade)context.getEJBObject()).queueOrderOnePhase(orderID);
		}
	}
	
	
   /** 
	 * @see TradeServices#queueOrderOnePhase(Integer)
	 * Queue the Order identified by orderID to be processed in a One Phase commit
	 * 
	 * In short, this method is deployed as TXN REQUIRES NEW to avoid a 
	 * 2-phase commit transaction across Entity and MDB access
	 * 
	 */

	public void queueOrderOnePhase(Integer orderID)
	    	throws javax.jms.JMSException, Exception
	{
		if (Log.doTrace() ) Log.trace("TradeBean:queueOrderOnePhase", orderID);
		queueOrderInternal(orderID, false);
	}
	
	class quotePriceComparator implements java.util.Comparator {
   		public int compare(Object quote1, Object quote2)
   		{
   			double change1 = ((LocalQuote) quote1).getChange();
   			double change2 = ((LocalQuote) quote2).getChange();
   			return new Double(change2).compareTo(new Double(change1));
   		}
   	}		

	public MarketSummaryDataBean getMarketSummary()
	throws Exception {
		

		MarketSummaryDataBean marketSummaryData = null;		
		try
		{
			if (Log.doTrace() ) {
				Log.trace("TradeBean:getMarketSummary -- getting market summary");
			}

			//Find Trade Stock Index Quotes (Top 100 quotes) 
			//ordered by their change in value
			Collection quotes=null;
			if ( orderBySQLSupported )
				quotes = quoteHome.findTSIAQuotesOrderByChange();
			else
				quotes = quoteHome.findTSIAQuotes();

			//SORT by price change the collection of stocks if the AppServer
			//     does not support the "ORDER BY" SQL clause
			if (! orderBySQLSupported) {
				//if (Log.doTrace()) 
					Log.trace("TradeBean:getMarketSummary() -- Sorting TSIA quotes");
		        ArrayList sortedQuotes = new ArrayList(quotes);
			    java.util.Collections.sort(sortedQuotes, new quotePriceComparator());	
			    quotes = sortedQuotes;
			}
		    //SORT END 
		    Object[] quoteArray = quotes.toArray();
		    ArrayList topGainers = new ArrayList(5);
		    ArrayList topLosers = new ArrayList(5);		    
		    for (int i=0; i<5; i++) topGainers.add(quoteArray[i]);
		    for (int i=quoteArray.length-1; i>=quoteArray.length-5; i--) topLosers.add(quoteArray[i]);
		    
			BigDecimal TSIA = FinancialUtils.ZERO;
			BigDecimal openTSIA = FinancialUtils.ZERO;			
			double totalVolume = 0.0;
			for (int i=0; i<quoteArray.length; i++) 
			{
				LocalQuote quote = (LocalQuote)quoteArray[i];
			  	BigDecimal price = quote.getPrice();
			  	BigDecimal open  = quote.getOpen();
			  	double volume = quote.getVolume();
			  	TSIA = TSIA.add(price);
			  	openTSIA = openTSIA.add(open);			  	
			  	totalVolume += volume;
			}
			TSIA = TSIA.divide(new BigDecimal(quoteArray.length), FinancialUtils.ROUND);
			openTSIA = openTSIA.divide(new BigDecimal(quoteArray.length), FinancialUtils.ROUND);
			
			/* This is an alternate approach using ejbSelect methods
			 *   In this approach an ejbSelect is used to select only the 
			 *   current price and open price values for the TSIA
				LocalQuote quote = quoteHome.findOne();
				BigDecimal TSIA = quote.getTSIA();
				openTSIA = quote.getOpenTSIA();
				Collection topGainers = quote.getTopGainers(5);
				Collection topLosers = quote.getTopLosers(5);
				LocalQuote quote = (LocalQuote)topGainers.iterator().next();
 	 			double volume = quote.getTotalVolume();							
			 *
			 */			

			// Convert the collections of topGainer/topLoser entities 			 
			// to collections of QuoteDataBeans
			Collection topGainersData = getDataBeansCollection(topGainers);
			Collection topLosersData = getDataBeansCollection(topLosers);			

			marketSummaryData = new MarketSummaryDataBean(TSIA, openTSIA, totalVolume, topGainersData, topLosersData);
		}
		catch (Exception e)
		{
			Log.error("TradeBean:getMarketSummary", e);
			throw new EJBException("TradeBean:getMarketSummary -- error ", e);
		}
		return marketSummaryData;
	}

	public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price)
			throws CreateException, Exception
	{
		try 
		{
			LocalQuote quote = quoteHome.create(symbol, companyName, price);
			QuoteDataBean quoteData = quote.getDataBean();
			if (Log.doTrace()) Log.trace("TradeBean:createQuote-->" + quoteData);
			return quoteData;
		}
		catch (Exception e)
		{
			Log.error("TradeBean:createQuote -- exception creating Quote", e);
			throw new EJBException(e);
		}
	}

	public QuoteDataBean getQuote(String symbol)
		throws Exception
	{

		if (Log.doTrace()) Log.trace("TradeBean:getQuote", symbol);
		QuoteDataBean quoteData;
		try {
			LocalQuote quote = quoteHome.findByPrimaryKey(symbol);
			quoteData = quote.getDataBean();
		} catch (FinderException fe) {
				//Cannot find quote for given symbol
				Log.error("TradeBean:getQuote--> Symbol: " + symbol + " cannot be found");
				BigDecimal z = new BigDecimal(0.0);
				quoteData = new QuoteDataBean("Error: Symbol " + symbol + " not found", "", 0.0, z, z, z, z, 0.0 );
		}
		return quoteData;
	}

	public Collection getAllQuotes() 
		throws Exception	
	{
		if (Log.doTrace()) Log.trace("TradeBean:getAllQuotes");

		Collection quoteBeans = new ArrayList();

		try {
			Collection quotes = quoteHome.findAll();
			for (Iterator it = quotes.iterator(); it.hasNext(); ) {
				LocalQuote quote = (LocalQuote)it.next();
				quoteBeans.add(quote.getDataBean());
			}
		}
		catch (FinderException fe) {
			Log.error("TradeBean:getAllQuotes");
		}
		return quoteBeans;
	}

	public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal changeFactor, double sharesTraded) 
		throws Exception
	{
		
		if ( TradeConfig.getUpdateQuotePrices() == false ) 
			return new QuoteDataBean();	
		
		if (Log.doTrace()) 
			Log.trace("TradeBean:updateQuote", symbol, changeFactor);	
		
		QuoteDataBean quoteData;
		try {
			LocalQuote quote = quoteHome.findByPrimaryKeyForUpdate(symbol);
			BigDecimal oldPrice = quote.getPrice();

			if (quote.getPrice().equals(TradeConfig.PENNY_STOCK_PRICE)) {
				changeFactor = TradeConfig.PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER;
			}

			BigDecimal newPrice = changeFactor.multiply(oldPrice).setScale(2, BigDecimal.ROUND_HALF_UP);

			quote.updatePrice(newPrice);
			quote.addToVolume(sharesTraded);
			quoteData = quote.getDataBean();
			((Trade)context.getEJBObject()).publishQuotePriceChange(quoteData, oldPrice, changeFactor, sharesTraded);

		} catch (FinderException fe) {
			//Cannot find quote for given symbol
			Log.error("TradeBean:updateQuotePriceVolume--> Symbol: " + symbol + " cannot be found");
			quoteData = new QuoteDataBean("Error: Symbol " + symbol + " not found");
		}
		return quoteData;
	}
	
	public void publishQuotePriceChange(QuoteDataBean quoteData, BigDecimal oldPrice, BigDecimal changeFactor, double sharesTraded)
	throws Exception
	{
		if ( publishQuotePriceChange == false)
			return;
		if (Log.doTrace())
			Log.trace("TradeBean:publishQuotePricePublishing -- quoteData = " + quoteData);		

		Connection conn = null;
		Session sess = null;
		
		try
		{			
			conn = tConnFactory.createConnection();		            
			sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer msgProducer = sess.createProducer(streamerTopic);
			TextMessage message = sess.createTextMessage();

			String command = "updateQuote";
			message.setStringProperty("command", command);
			message.setStringProperty("symbol",  quoteData.getSymbol() );
			message.setStringProperty("company", quoteData.getCompanyName() );		
			message.setStringProperty("price",   quoteData.getPrice().toString());
			message.setStringProperty("oldPrice",oldPrice.toString());		
			message.setStringProperty("open",    quoteData.getOpen().toString());
			message.setStringProperty("low",     quoteData.getLow().toString());
			message.setStringProperty("high",    quoteData.getHigh().toString());
			message.setDoubleProperty("volume",  quoteData.getVolume());		
					
			message.setStringProperty("changeFactor", changeFactor.toString());		
			message.setDoubleProperty("sharesTraded", sharesTraded);				
			message.setLongProperty("publishTime", System.currentTimeMillis());					
			message.setText("Update Stock price for " + quoteData.getSymbol() + " old price = " + oldPrice + " new price = " + quoteData.getPrice());

			msgProducer.send(message);
		}
		catch (Exception e)
		{
			throw e; // pass the exception back
		}	
		finally {	
			if (conn != null)
				conn.close();	
			if (sess != null)
			sess.close();		
		}
	}	

	public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode)
	throws Exception 
	{

		LocalOrder order=null;
		BigDecimal total;		
		try {
			if (Log.doTrace()) 
				Log.trace("TradeBean:buy", userID, symbol, new Double(quantity), new Integer(orderProcessingMode));					
		/*  The following commented code shows alternative forms of the finder needed for this
		 *  method
		 *  The first alternative requires a 2-table join. Some database cannot allocate an Update
		 *  Lock on a join select.
		 * 
		 *  The second alternative shows the finder being executed without allocation an update
		 *  lock on the row. Normally, an update lock would not be necessary, but is required if
		 *  the same user logs in multiple times to avoid a deadlock situation.
		 * 
		 *  The third alternative runs the finder and allocates an update lock on the row(s)
		 * 			
			LocalAccount account = accountHome.findByUserIDForUpdate(userID);	
 
			LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKey(userID)).getAccountForUpdate();
		 */
			LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKeyForUpdate(userID)).getAccountForUpdate();

			LocalQuote quote = quoteHome.findByPrimaryKey(symbol);
			LocalHolding holding = null;  //The holding will be created by this buy order
			Integer orderID  = keySequence.getNextID("order");

			order = createOrder(orderID, account, quote, holding, "buy", quantity);

			//UPDATE - account should be credited during completeOrder
			BigDecimal price = quote.getPrice();
			BigDecimal orderFee = order.getOrderFee();
			BigDecimal balance = account.getBalance();
			total   = (new BigDecimal(quantity).multiply(price)).add(orderFee);
			account.setBalance(balance.subtract(total));			
			
			if (orderProcessingMode == TradeConfig.SYNCH) 
				completeOrderInternal(order.getOrderID());
			else if (orderProcessingMode == TradeConfig.ASYNCH)
				// Invoke the queueOrderOnePhase method w/ TXN requires new attribute
				// to side-step a 2-phase commit across DB and JMS access
				queueOrder(order.getOrderID(), false);
			else //TradeConfig.ASYNC_2PHASE
				queueOrder(order.getOrderID(), true);
		}
		catch (Exception e)
		{
			Log.error("TradeBean:buy("+userID+","+symbol+","+quantity+") --> failed", e);
			/* On exception - cancel the order */
			if (order != null) order.cancel();
			throw new EJBException(e);
		}	
		return order.getDataBean();
	}

	public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) 
	throws Exception 
	{
		
		LocalOrder order=null;
		BigDecimal total;		
		try {
			if (Log.doTrace()) 
				Log.trace("TradeBean:sell", userID, holdingID, new Integer(orderProcessingMode));					
			
			/* Some databases cannot allocate an update lock on a JOIN
			 * use the second approach below to acquire update lock
			LocalAccount account = accountHome.findByUserIDForUpdate(userID);
			*/
		 
			LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKeyForUpdate(userID)).getAccountForUpdate();
			LocalHolding holding;
			try {
				holding = holdingHome.findByPrimaryKeyForUpdate(holdingID);
			}
			catch (ObjectNotFoundException oe)
			{
				Log.error("TradeBean:sell User " + userID + " attempted to sell holding " + holdingID + " which has already been sold");
				OrderDataBean orderData = new OrderDataBean();
				orderData.setOrderStatus("cancelled");
				return orderData;
			}
			LocalQuote   quote = holding.getQuote();
			double		 quantity = holding.getQuantity();
			Integer orderID = keySequence.getNextID("order");
			order = createOrder(orderID, account, quote, holding, "sell", quantity);

			//UPDATE the holding purchase data to signify this holding is "inflight" to be sold
			//    -- could add a new holdingStatus attribute to holdingEJB
			holding.setPurchaseDate(new java.sql.Timestamp(0));
			
			//UPDATE - account should be credited during completeOrder
			BigDecimal price = quote.getPrice();
			BigDecimal orderFee = order.getOrderFee();
			BigDecimal balance = account.getBalance();	
			total   = (new BigDecimal(quantity).multiply(price)).subtract(orderFee);				
			account.setBalance(balance.add(total));
			
			if (orderProcessingMode == TradeConfig.SYNCH) 
				completeOrderInternal(order.getOrderID());
			else if (orderProcessingMode == TradeConfig.ASYNCH)
				queueOrder(order.getOrderID(), false);
			else //TradeConfig.ASYNC_2PHASE
				queueOrder(order.getOrderID(), true);

		}
		catch (Exception e)
		{		
			Log.error("TradeBean:sell("+userID+","+holdingID+") --> failed", e);
			if (order != null) order.cancel();
			//UPDATE - handle all exceptions like:
			throw new EJBException("TradeBean:sell("+userID+","+holdingID+")",e);	
		}
		return order.getDataBean();
	}


	public Collection getOrders(String userID) 
		throws FinderException, Exception
	{
		
		if (Log.doTrace()) 
			Log.trace("TradeBean:getOrders", userID);

		/*  The following commented code shows alternative forms of the finder needed for this
		 *  method
		 *  The first alternative requires a 2-table join. Some database cannot allocate an Update
		 *  Lock on a join select.
		 * 
		 *  The second alternative shows the finder being executed without allocation an update
		 *  lock on the row. Normally, an update lock would not be necessary, but is required if
		 *  the same user logs in multiple times to avoid a deadlock situation.
		 * 
		 *  The third alternative runs the finder and allocates an update lock on the row(s)
		 * 					
		
			Collection orders = accountHome.findByUserID(userID).getOrders();
			
			LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKey(userID)).getAccountForUpdate();
		 */
		LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKeyForUpdate(userID)).getAccountForUpdate();
		Collection orders = account.getOrders();

		ArrayList dataBeans = new ArrayList();
		if ( orders == null ) return dataBeans;
		
		Iterator it = orders.iterator();
		//TODO: return top 5 orders for now -- next version will add a getAllOrders method
		//      also need to get orders sorted by order id descending
		int i=0;
		while ( (it.hasNext()) && (i++ < 5)) 
			dataBeans.add(((LocalOrder) it.next()).getDataBean());
		
		return dataBeans;
	}

	public Collection getClosedOrders(String userID) 
		throws FinderException, Exception
	{
		if (Log.doTrace()) 
			Log.trace("TradeBean:getClosedOrders", userID);

		ArrayList dataBeans = new ArrayList();		
		try {

		/*  The following commented code shows alternative forms of the finder needed for this
		 *  method
		 *  The first alternative requires a 2-table join. Some database cannot allocate an Update
		 *  Lock on a join select.
		 * 
		 *  The second alternative shows the finder being executed without allocation an update
		 *  lock on the row. Normally, an update lock would not be necessary, but is required if
		 *  the same user logs in multiple times to avoid a deadlock situation.
		 * 
		 *  The third alternative runs the finder and allocates an update lock on the row(s)
		 * 			
			Collection orders = orderHome.findClosedOrdersForUpdate(userID);	 	
		 *
  		 	LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKey(userID)).getAccount();							
		 */

  		 	LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKeyForUpdate(userID)).getAccountForUpdate();				  		 	
  		 	//Get the primary keys for all the closed Orders for this account.
  		 	Collection ordersKeys = account.getClosedOrders();
			if ( ordersKeys == null ) return dataBeans;
		
			Iterator it = ordersKeys.iterator();
			while (it.hasNext())
			{
				Integer orderKey = (Integer) it.next();
				LocalOrder order = (LocalOrder) orderHome.findByPrimaryKeyForUpdate(orderKey);
				//Complete the order 
				order.setOrderStatus("completed");
				dataBeans.add(order.getDataBean());
			}
			
		}
		catch (Exception e)
		{
			Log.error("TradeBean.getClosedOrders", e);
			throw new EJBException("TradeBean.getClosedOrders - error", e);
		}
		return dataBeans;
	}
	

	public OrderDataBean completeOrder(Integer orderID, boolean twoPhase)
	throws Exception {
		if (Log.doTrace()) Log.trace("TradeBean:completeOrder", orderID + " twoPhase="+twoPhase);
		if (twoPhase)
			return completeOrderInternal(orderID);
		else
		{
			// invoke the completeOrderOnePhase -- which requires a new transaction
			// the completeOrder will run in it's own transaction thus not requiring a 
			// 2-phase commit
			return ((Trade)context.getEJBObject()).completeOrderOnePhase(orderID);
		}
	}
	
	//completeOrderOnePhase method is deployed w/ TXN_REQUIRES_NEW
	//thus the completeOrder call from the MDB should not require a 2-phase commit
	public OrderDataBean completeOrderOnePhase(Integer orderID)
	throws Exception {
		if (Log.doTrace()) Log.trace("TradeBean:completeOrderOnePhase", orderID);		
		return completeOrderInternal(orderID);
	}

	private OrderDataBean completeOrderInternal(Integer orderID) 
			throws Exception
	{

		LocalOrder order = orderHome.findByPrimaryKeyForUpdate(orderID);
		
		if (order == null)
		{
			Log.error("TradeBean:completeOrderInternal  -- Unable to find Order " + orderID + " FBPK returned " + order);
			order.cancel();			
			return order.getDataBean();
		}

		String orderType = order.getOrderType();
		String orderStatus = order.getOrderStatus();

		if (order.isCompleted())
			throw new EJBException("Error: attempt to complete Order that is already completed\n" + order);

		LocalAccount account = order.getAccount();
		LocalQuote     quote = order.getQuote();
		LocalHolding holding = order.getHoldingForUpdate();
		BigDecimal     price = order.getPrice();
		double	    quantity = order.getQuantity();
		BigDecimal  orderFee = order.getOrderFee();

		BigDecimal balance = account.getBalance();

		/* 
		 * 	getProfile is marked as Pess. Update to get a DB lock
		 * Here we invoke getProfileForRead which is deployed to not
		 * lock the DB (Pess. Read)
		 */	 
		 String userID = account.getProfile().getUserID();
		 	
		/*	 
		 * total = (quantity * purchasePrice) + orderFee
		 */

			
		if (Log.doTrace()) Log.trace(
					"TradeBeanInternal:completeOrder--> Completing Order " + order.getOrderID()
					 + "\n\t Order info: "   +   order
					 + "\n\t Account info: " + account
					 + "\n\t Quote info: "   +   quote
					 + "\n\t Holding info: " + holding);
			

		if (order.isBuy()) {
			/* Complete a Buy operation
			 *	- create a new Holding for the Account
			 *	- deduct the Order cost from the Account balance
			 */

			LocalHolding newHolding = createHolding(account, quote, quantity, price);
			order.setHolding(newHolding);
		}
		
		if (order.isSell()) {
			/* Complete a Sell operation
			 *	- remove the Holding from the Account
			 *	- deposit the Order proceeds to the Account balance
			 */
			if ( holding == null )
			{
				Log.error("TradeBean:completeOrderInternal -- Unable to sell order " + order.getOrderID() + " holding already sold");
				order.cancel();
				return order.getDataBean();
			}
			else
			{	
				holding.remove();
				holding = null;
			}

			// This is managed by the container
			// order.setHolding(null);

		}
		order.setOrderStatus("closed");

		order.setCompletionDate(new java.sql.Timestamp(System.currentTimeMillis()));
		
		if (Log.doTrace()) Log.trace(
					"TradeBean:completeOrder--> Completed Order " + order.getOrderID()
					 + "\n\t Order info: "   +   order
					 + "\n\t Account info: " + account
					 + "\n\t Quote info: "   +   quote
					 + "\n\t Holding info: " + holding);

		BigDecimal priceChangeFactor = TradeConfig.getRandomPriceChangeFactor();
		if(Log.doTrace())
			Log.trace("Calling TradeAction:orderCompleted from Session EJB using Session Object");		
			//FUTURE All getEJBObjects could be local -- need to add local I/F

		TradeServices trade = (TradeServices)context.getEJBObject();
		TradeAction tradeAction = new TradeAction(trade);

		//signify this order for user userID is complete
		tradeAction.orderCompleted(userID, orderID);
		return order.getDataBean();
	}
	
	
	//These methods are used to provide the 1-phase commit runtime option for TradeDirect
	// Basically these methods are deployed as txn requires new and invoke TradeDirect methods
	// There is no mechanism outside of EJB to start a new transaction
    public OrderDataBean completeOrderOnePhaseDirect(Integer orderID)
    throws Exception {
    	if (Log.doTrace())
    		Log.trace("TradeBean:completeOrderOnePhaseDirect -- completing order by calling TradeDirect orderID=" +orderID);
    	return (new org.apache.geronimo.samples.daytrader.direct.TradeDirect()).completeOrderOnePhase(orderID);
    }
	public void cancelOrderOnePhaseDirect(Integer orderID) 
	throws Exception {
    	if (Log.doTrace())
    		Log.trace("TradeBean:cancelOrderOnePhaseDirect -- cancelling order by calling TradeDirect orderID=" +orderID);
		(new org.apache.geronimo.samples.daytrader.direct.TradeDirect()).cancelOrderOnePhase(orderID);
	}
	
	
	public void cancelOrder(Integer orderID, boolean twoPhase)
	throws Exception {
		if (Log.doTrace()) Log.trace("TradeBean:cancelOrder", orderID + " twoPhase="+twoPhase);
		if (twoPhase)
			cancelOrderInternal(orderID);
		else
		{
			// invoke the cancelOrderOnePhase -- which requires a new transaction
			// the completeOrder will run in it's own transaction thus not requiring a 
			// 2-phase commit
			((Trade)context.getEJBObject()).cancelOrderOnePhase(orderID);
		}
	}

	//cancelOrderOnePhase method is deployed w/ TXN_REQUIRES_NEW
	//thus the completeOrder call from the MDB should not require a 2-phase commit
	public void cancelOrderOnePhase(Integer orderID)
	throws Exception {
		if (Log.doTrace()) Log.trace("TradeBean:cancelOrderOnePhase", orderID);		
		cancelOrderInternal(orderID);
	}
	
	
	private void cancelOrderInternal(Integer orderID)
	throws Exception
	{
		LocalOrder order = orderHome.findByPrimaryKeyForUpdate(orderID);
		order.cancel();
	}
	
		
	public void orderCompleted(String userID, Integer orderID) 
	throws Exception
	{
		throw new UnsupportedOperationException("TradeBean:orderCompleted method not supported");
	}
	public LocalHolding createHolding(
		LocalAccount account,
		LocalQuote quote,
		double quantity,
		BigDecimal purchasePrice) 
		throws Exception		
	{
		LocalHolding newHolding = null;
		Integer holdingID = null;
		try {

		if (Log.doTrace())
			Log.trace("TradeBean:createHolding");

			holdingID = keySequence.getNextID("holding");
			newHolding =
				holdingHome.create(holdingID, account, quote, quantity, purchasePrice);
		} catch (Exception e) {
			String error = "Failed to create Holding for account: " + account.getAccountID() 
							+ " with quote: " + quote.getSymbol() 
							+ " holdingID: " + holdingID 
							+ " quantity: " + quantity + "\n";
			Log.error(e, error );
			throw new EJBException(error,e);
		}
		return newHolding;
	}

	public Collection getHoldings(String userID) 
		throws FinderException, Exception
	{
		if (Log.doTrace())
			Log.trace("TradeBean:getHoldings", userID);
		Collection holdings = holdingHome.findByUserID(userID);
		if (Log.doTrace())
			Log.trace("Got holdings collection size="+holdings.size());		
		ArrayList dataBeans = new ArrayList();
		if ( holdings == null ) return dataBeans;
	
		Iterator it = holdings.iterator();
		while (it.hasNext()) {
			HoldingDataBean holdingData = ((LocalHolding) it.next()).getDataBean();
			dataBeans.add(holdingData);
		}
		return dataBeans;
	}
	
	public HoldingDataBean getHolding(Integer holdingID) 
		throws FinderException, Exception
	{
		if (Log.doTrace())
			Log.trace("TradeBean:getHolding", holdingID);
		LocalHolding holding = holdingHome.findByPrimaryKey(holdingID);
		HoldingDataBean holdingData = holding.getDataBean();		
		if (Log.doTrace())
			Log.trace("TradeBean:getHolding " + holdingData);		
		return holdingData;
	}



	public LocalOrder createOrder(
		int orderID,
		LocalAccount account,
		LocalQuote quote,
		LocalHolding holding,
		String orderType,
		double quantity)
	throws Exception {
		try
		{
			return createOrder(new Integer(orderID), account, quote, holding, orderType, quantity);
		}
		catch(Exception e)
		{
			Log.error("TradeBean:createOrder -- failed to create Order", e);
			throw new EJBException("TradeBean:createOrder -- failed to create Order", e);
		}
	}

	public LocalOrder createOrder(
		Integer orderID,
		LocalAccount account,
		LocalQuote quote,
		LocalHolding holding,
		String orderType,
		double quantity)
	throws CreateException, Exception {

		LocalOrder order = null;
		
		if (Log.doTrace() )
			Log.trace(
			"TradeBean:createOrder(orderID="
				+ orderID
				+ " account="
				+ ((account == null) ? null : account.getPrimaryKey())
				+ " quote="
				+ ((quote == null) ? null : quote.getPrimaryKey())
				+ " orderType="
				+ orderType
				+ " quantity="
				+ quantity);
		try
		{			
			order =	orderHome.create(orderID, account, quote, holding, orderType, quantity);
		}
		catch(Exception e)
		{
			Log.error("TradeBean:createOrder -- failed to create Order", e);
			throw new EJBException("TradeBean:createOrder -- failed to create Order", e);
		}		
		return order;
	}

	public AccountDataBean login(String userID, String password)
	throws FinderException, Exception {
		
		/*  The following commented code shows alternative forms of the finder needed for this
		 *  method
		 *  The first alternative requires a 2-table join. Some database cannot allocate an Update
		 *  Lock on a join select.
		 * 
		 *  The second alternative shows the finder being executed without allocation an update
		 *  lock on the row. Normally, an update lock would not be necessary, but is required if
		 *  the same user logs in multiple times to avoid a deadlock situation.
		 * 
		 *  The third alternative runs the finder and allocates an update lock on the row(s)
		 *
			LocalAccount account = accountHome.findByUserIDForUpdate(userID);
		 
		    LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKey(userID)).getAccountForUpdate();
		 */
		 LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKeyForUpdate(userID)).getAccountForUpdate();
		 
		if (Log.doTrace())
			Log.trace("TradeBean:login", userID, password);
		account.login(password);
		AccountDataBean accountData = account.getDataBean();
		if (Log.doTrace())
			Log.trace("TradeBean:login(" + userID + "," + password + ") success" + accountData);
		return accountData;
	}

	public void logout(String userID)
	throws FinderException, Exception {
		if (Log.doTrace())
			Log.trace("TradeBean:logout", userID);
			
		/*  The following commented code shows alternative forms of the finder needed for this
		 *  method
		 *  The first alternative requires a 2-table join. Some database cannot allocate an Update
		 *  Lock on a join select.
		 * 
		 *  The second alternative shows the finder being executed without allocation an update
		 *  lock on the row. Normally, an update lock would not be necessary, but is required if
		 *  the same user logs in multiple times to avoid a deadlock situation.
		 * 
		 *  The third alternative runs the finder and allocates an update lock on the row(s)
		 
		 * 	LocalAccount account = accountHome.findByUserIDForUpdate(userID);
			LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKey(userID)).getAccountForUpdate();
		 *
		 */
		LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKeyForUpdate(userID)).getAccountForUpdate();
		
		
		if (Log.doTrace()) Log.trace("TradeBean:logout(" + userID + ") success");
		account.logout();
	}

	public AccountDataBean register(
		String userID,
		String password,
		String fullname,
		String address,
		String email,
		String creditcard,
		BigDecimal openBalance)
		throws CreateException, Exception {

		LocalAccount account = null;

		try {
		if (Log.doTrace())
			Log.trace("TradeBean:register", userID, password, fullname, address, email, creditcard, openBalance);
			
			Integer accountID = keySequence.getNextID("account");
			account =
				accountHome.create(
					accountID,
					userID,
					password,
					openBalance,
					fullname,
					address,
					email,
					creditcard);
		}
		catch (Exception e)
		{
				Log.error("Failed to register new Account\n" + e);
				throw new EJBException("Failed to register new Account\n", e);
		}	
		AccountDataBean accountData = account.getDataBean();		
		return accountData;
	}

	public AccountDataBean getAccountData(String userID)
	throws FinderException, Exception {

		if (Log.doTrace())
			Log.trace("TradeBean:getAccountData", userID);

		/*  The following commented code shows alternative forms of the finder needed for this
		 *  method
		 *  The first alternative requires a 2-table join. Some database cannot allocate an Update
		 *  Lock on a join select.
		 * 
		 *  The second alternative shows the finder being executed without allocation an update
		 *  lock on the row. Normally, an update lock would not be necessary, but is required if
		 *  the same user logs in multiple times to avoid a deadlock situation.
		 * 
		 *  The third alternative runs the finder and allocates an update lock on the row(s)
		 * 					
			LocalAccount account = accountHome.findByUserID(userID);
			
			LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKey(userID)).getAccountForUpdate();
		 */
		LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKeyForUpdate(userID)).getAccountForUpdate();

		AccountDataBean accountData = account.getDataBean();
		return accountData;
	}
	public AccountProfileDataBean getAccountProfileData(String userID)
	throws FinderException, Exception {
		if (Log.doTrace())
			Log.trace("TradeBean:getAccountProfileData", userID);

		/*  The following commented code shows alternative forms of the finder needed for this
		 *  method
		 *  The first alternative requires a 2-table join. Some database cannot allocate an Update
		 *  Lock on a join select.
		 * 
		 *  The second alternative shows the finder being executed without allocation an update
		 *  lock on the row. Normally, an update lock would not be necessary, but is required if
		 *  the same user logs in multiple times to avoid a deadlock situation.
		 * 
		 *  The third alternative runs the finder and allocates an update lock on the row(s)
		 * 					
			LocalAccount account = accountHome.findByUserID(userID);
			
			LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKey(userID)).getAccountForUpdate();
		 */
		LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKeyForUpdate(userID)).getAccountForUpdate();

		AccountProfileDataBean accountProfileData = account.getProfileDataBean();
		return accountProfileData;
	}

	public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean accountProfileData)
	throws FinderException, Exception {
		
		if (Log.doTrace())
			Log.trace("TradeBean:updateAccountProfileData", accountProfileData);
		
		LocalAccount account = ((LocalAccountProfile) profileHome.findByPrimaryKeyForUpdate(accountProfileData.getUserID())).getAccountForUpdate();

		accountProfileData =
			account.updateAccountProfile(accountProfileData);
		return accountProfileData;
	}
	
	public RunStatsDataBean resetTrade(boolean deleteAll)
	throws Exception
	{
		if (Log.doTrace())
			Log.trace("TradeBean:resetTrade", new Boolean(deleteAll));

		//Clear MDB Statistics		
		MDBStats.getInstance().reset();
		// Reset Trade
		return new org.apache.geronimo.samples.daytrader.direct.TradeDirect().resetTrade(deleteAll);
	}

	private Collection getDataBeansCollection(Collection entities)
	{
		ArrayList dataBeans = new ArrayList();
	
		if ( (entities == null) || (entities.size()<= 0) )
		return dataBeans;

		Iterator it = entities.iterator();
		while (it.hasNext()) {
			LocalQuote entity = (LocalQuote) it.next();
			Object o = (Object)entity.getDataBean();
			dataBeans.add(o);
		}
		return dataBeans;
	}

	/**
	 * provides a simple session method with no database access to test performance of a simple
	 * path through a stateless session 
	 * @param investment amount
	 * @param NetValue current value
	 * @return return on investment as a percentage
	 */
	public double investmentReturn(double investment, double NetValue)
		throws Exception
	{
		if (Log.doTrace())
			Log.trace("TradeBean:investmentReturn");
		
		double diff = NetValue - investment;
		double ir = diff / investment;
		return ir;

	}
	
	/**
	 * This method provides a ping test for a 2-phase commit operation
	 * 
	 * @param symbol to lookup
	 * @return quoteData after sending JMS message
	 */	
	public QuoteDataBean pingTwoPhase(String symbol)
	throws Exception
	{
		if (Log.doTrace()) Log.trace("TradeBean:pingTwoPhase", symbol);
		QuoteDataBean quoteData=null;
		Connection conn = null;
		Session sess = null;		
		try {
			
			//Get a Quote and send a JMS message in a 2-phase commit
			LocalQuote quote = quoteHome.findByPrimaryKey(symbol);
			quoteData = quote.getDataBean();

			conn = qConnFactory.createConnection();                        
			sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer msgProducer = sess.createProducer(queue);
			TextMessage   message = sess.createTextMessage();

			String command= "ping";
			message.setStringProperty("command", command);
			message.setLongProperty("publishTime", System.currentTimeMillis());			
			message.setText("Ping message for queue java:comp/env/jms/TradeBrokerQueue sent from TradeSessionEJB:pingTwoPhase at " + new java.util.Date());

			msgProducer.send(message);	
		} 
		catch (Exception e) {
			Log.error("TradeBean:pingTwoPhase -- exception caught",e);
		}

		finally {
			if (conn != null)
				conn.close();	
			if (sess != null)
				sess.close();
		}			
		
		return quoteData;
	}	


    /* Required javax.ejb.SessionBean interface methods */

	public TradeBean() {
	}

	private static boolean warnJMSFailure = true;
	public void ejbCreate() throws CreateException {
		try {

			if (Log.doTrace())
				Log.trace("TradeBean:ejbCreate  -- JNDI lookups of EJB and JMS resources");

			InitialContext ic = new InitialContext();
			quoteHome 		= (LocalQuoteHome)   ic.lookup("java:comp/env/ejb/Quote");
			accountHome 	= (LocalAccountHome) ic.lookup("java:comp/env/ejb/Account");
			profileHome 	= (LocalAccountProfileHome) ic.lookup("java:comp/env/ejb/AccountProfile");
			holdingHome 	= (LocalHoldingHome) ic.lookup("java:comp/env/ejb/Holding");
			orderHome 		= (LocalOrderHome)   ic.lookup("java:comp/env/ejb/Order");
			keySequenceHome = (LocalKeySequenceHome) ic.lookup("java:comp/env/ejb/KeySequence");			

			orderBySQLSupported = ( (Boolean) ic.lookup("java:comp/env/orderBySQLSupported") ).booleanValue();
			publishQuotePriceChange  = ( (Boolean) ic.lookup("java:comp/env/publishQuotePriceChange") ).booleanValue();
			updateQuotePrices  = ( (Boolean) ic.lookup("java:comp/env/updateQuotePrices") ).booleanValue();
			TradeConfig.setUpdateQuotePrices(updateQuotePrices);

			try
			{
				qConnFactory = (ConnectionFactory) ic.lookup("java:comp/env/jms/QueueConnectionFactory");
				queue = (Queue) ic.lookup("java:comp/env/jms/TradeBrokerQueue");
				tConnFactory = (ConnectionFactory) ic.lookup("java:comp/env/jms/TopicConnectionFactory");
				streamerTopic = (Topic) ic.lookup("java:comp/env/jms/TradeStreamerTopic");
			}
			catch (Exception e)
			{
				if (warnJMSFailure == true)
				{
					warnJMSFailure = false;
					Log.error("TradeBean:ejbCreate  Unable to lookup JMS Resources\n\t -- Asynchronous mode will not work correctly and Quote Price change publishing will be disabled", e);
				}				
				publishQuotePriceChange = false;			
			}		
			
		} catch (Exception e) {
			Log.error("TradeBean:ejbCreate: Lookup of Local Entity Homes Failed\n" + e);
			e.printStackTrace();
			//UPDATE
			//throw new CreateException(e.toString());
		}

		if ((quoteHome 	== null) ||
			(     accountHome	== null) ||
			(     holdingHome	== null) ||
			(       orderHome	== null) ||
			( keySequenceHome	== null) //||			 		
//			(    qConnFactory	== null) ||
//			(           queue	== null) ||
//			(    tConnFactory	== null) ||
//			(   streamerTopic	== null) ||
			)
			{
				String error = "TradeBean:ejbCreate()  JNDI lookup of Trade resource failed\n" +
					"\n\t quoteHome="+quoteHome+
					"\n\t accountHome="+ accountHome+
					"\n\t holdingHome="+ holdingHome+
					"\n\t orderHome="+ orderHome+
					"\n\t qConnFactory="+ qConnFactory+
					"\n\t queue="+ queue+
					"\n\t tConnFactory="+ tConnFactory+
					"\n\t streamerTopic="+ streamerTopic;
				Log.error(error);
				//UPDATE
				//throw new EJBException(error);
			}
		keySequence = keySequenceHome.create();
	}

	public void ejbRemove() 
	{
		try
		{
			if (Log.doTrace())
				Log.trace("TradeBean:ejbRemove");
		}
		catch (Exception e)
		{
			Log.error(e,"Unable to close Queue or Topic connection on Session EJB remove");
		}
	}
	public void ejbActivate() {
	}
	public void ejbPassivate() {
	}

	public void setSessionContext(SessionContext sc) {
		context = sc;
	}
}
