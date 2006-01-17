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

import javax.ejb.EJBObject;
import java.rmi.Remote;
import org.apache.geronimo.samples.daytrader.*;

public interface Trade extends EJBObject, TradeServices, Remote {

   /**
	 * Queue the Order identified by orderID to be processed in a One Phase commit
	 * 
	 * In short, this method is deployed as TXN REQUIRES NEW to avoid a 
	 * 2-phase commit transaction across Entity and MDB access
	 * 
	 * Orders are submitted through JMS to a Trading Broker
	 * and completed asynchronously. This method queues the order for processing
	 *
	 * @param orderID the Order being queued for processing
	 * @return OrderDataBean providing the status of the completed order
	 */
	public void queueOrderOnePhase(Integer orderID) throws Exception;
   /**
	 * Complete the Order identified by orderID in a One Phase commit
	 * 
	 * In short, this method is deployed as TXN REQUIRES NEW to avoid a 
	 * 2-phase commit transaction across Entity and MDB access
	 * 
	 * Orders are submitted through JMS to a Trading agent
	 * and completed asynchronously. This method completes the order
	 * For a buy, the stock is purchased creating a holding and the users account is debited
	 * For a sell, the stock holding is removed and the users account is credited with the proceeds
	 *
	 * @param orderID the Order to complete
	 * @return OrderDataBean providing the status of the completed order
	 */
	public OrderDataBean completeOrderOnePhase(Integer orderID) throws Exception;
	
   /**
	 * Complete the Order identified by orderID in a One Phase commit
	 * using TradeDirect to complete the Order
	 * 
	 * In short, this method is deployed as TXN REQUIRES NEW to avoid a 
	 * 2-phase commit transaction across DB and MDB access
	 * The EJB method is used only to start a new transaction so the direct runtime mode
	 * for the completeOrder will run in a 1-phase commit
	 * 
	 * Orders are submitted through JMS to a Trading agent
	 * and completed asynchronously. This method completes the order using TradeDirect
	 * For a buy, the stock is purchased creating a holding and the users account is debited
	 * For a sell, the stock holding is removed and the users account is credited with the proceeds
	 *
	 * @param orderID the Order to complete
	 * @return OrderDataBean providing the status of the completed order
	 */	
    public OrderDataBean completeOrderOnePhaseDirect(Integer orderID) throws Exception;

   /**
	 * Cancel the Order identefied by orderID
	 * 
	 * In short, this method is deployed as TXN REQUIRES NEW to avoid a 
	 * 2-phase commit transaction across Entity and MDB access
	 * 
	 * The boolean twoPhase specifies to the server implementation whether or not the
	 * method is to participate in a global transaction
	 *
	 * @param orderID the Order to complete
	 * @return OrderDataBean providing the status of the completed order
	 */
	public void cancelOrderOnePhase(Integer orderID) throws Exception;

   /**
	 * Cancel the Order identefied by orderID
	 * using TradeDirect to complete the Order
	 * 
	 * In short, this method is deployed as TXN REQUIRES NEW to avoid a 
	 * 2-phase commit transaction across DB and MDB access
	 * The EJB method is used only to start a new transaction so the direct runtime mode
	 * for the cancleOrder will run in a 1-phase commit
	 * 
	 * The boolean twoPhase specifies to the server implementation whether or not the
	 * method is to participate in a global transaction
	 *
	 * @param orderID the Order to complete
	 * @return OrderDataBean providing the status of the completed order
	 */
	public void cancelOrderOnePhaseDirect(Integer orderID) throws Exception;
	
   /**
	 * Publish to the QuoteChange Message topic when a stock
	 * price and volume are updated
	 * 
	 * This method is deployed as TXN REQUIRES NEW to avoid a 
	 * 2-phase commit transaction across the DB update and MDB access
	 * (i.e. a failure to publish will not cause the stock update to fail
	 *
	 * @param quoteData - the updated Quote
	 * @param oldPrice - the price of the Quote before the update
	 * @param sharesTraded - the quantity of sharesTraded
	 */
	public void publishQuotePriceChange(QuoteDataBean quoteData, java.math.BigDecimal oldPrice, java.math.BigDecimal changeFactor,  double sharesTraded) throws Exception;
	
	/**
	 * provides a simple session method with no database access to test performance of a simple
	 * path through a stateless session 
	 * @param investment amount
	 * @param NetValue current value
	 * @return return on investment as a percentage
	 */
	public double investmentReturn(double investment, double NetValue) throws Exception;	

	/**
	 * This method provides a ping test for a 2-phase commit operation
	 * 
	 * @param symbol to lookup
	 * @return quoteData after sending JMS message
	 */	
	public QuoteDataBean pingTwoPhase(String symbol) throws Exception;

}
