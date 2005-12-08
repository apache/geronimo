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

import org.apache.geronimo.samples.daytrader.direct.*;
import org.apache.geronimo.samples.daytrader.util.*;

import org.apache.geronimo.samples.daytrader.*;

public class TradeBrokerMDB 
	implements MessageDrivenBean, MessageListener {


    private transient MessageDrivenContext mdc = null;
    private Context context;
    
	private TradeHome tradeHome;
	
 	//Message send/receive timing Statistics
 	private MDBStats mdbStats;
 	private int statInterval = 100;

    public void onMessage(Message message) {
        try {
        	if (Log.doTrace()) 
        		Log.trace("TradeBroker:onMessage -- received message -->" 
        			+ ((TextMessage)message).getText() + "command-->" 
        			+	message.getStringProperty("command") + "<--");
			
			if (message.getJMSRedelivered())
			{
				Log.log("TradeBrokerMDB: The following JMS message was redelivered due to a rollback:\n"+ ((TextMessage)message).getText() );
				//Order has been cancelled -- ignore returned messages 
				return;        			
			}
        	String command = message.getStringProperty("command");
        	if (command==null) 
        	{
        		Log.debug("TradeBrokerMDB:onMessage -- received message with null command. Message-->"+message);
        		return;
	        }
            if (command.equalsIgnoreCase("neworder")) 
            {
            	/* Get the Order ID and complete the Order */
		        Integer orderID = new Integer(message.getIntProperty("orderID"));
		        boolean twoPhase = message.getBooleanProperty("twoPhase");
		        boolean direct = message.getBooleanProperty("direct");
        		long publishTime = message.getLongProperty("publishTime");
				long receiveTime = System.currentTimeMillis();			
		        

		        TradeServices trade = null;

		        try {
		        	trade = getTrade(direct);
				
					if (Log.doTrace()) 
						Log.trace("TradeBrokerMDB:onMessage - completing order " + orderID + " twoPhase=" +twoPhase + " direct="+direct);

					trade.completeOrder(orderID, twoPhase);
					
					TimerStat currentStats = mdbStats.addTiming("TradeBrokerMDB:neworder", publishTime, receiveTime );
					
					if ( (currentStats.getCount() % statInterval) == 0)
					{
							Log.log(new java.util.Date()+ "\nTradeBrokerMDB: processed 100 stock trading orders. " + 
  								"\nCurrent NewOrder Message Statistics\n\tTotal NewOrders process = " + currentStats.getCount() + 
								"\n\tTime to receive messages (in seconds):" +
								"\n\t\tmin: " +currentStats.getMinSecs()+
								"\n\t\tmax: " +currentStats.getMaxSecs()+
								"\n\t\tavg: " +currentStats.getAvgSecs()+
								"\n\n\n\tThe current order being processed is:\n\t"+((TextMessage)message).getText());
					}
		        }
		        catch (Exception e) 
		        {
		        	Log.error("TradeBrokerMDB:onMessage Exception completing order: " + orderID + "\n",  e);
       	            mdc.setRollbackOnly();
       	            /* UPDATE - order is cancelled in trade if an error is caught
		        	try
		        	{ 
		   	        	trade.cancelOrder(orderID, twoPhase);
		        	}
		        	catch (Exception e2)
		        	{
			        	Log.error("order cancel failed", e);
		        	}*/
		        }
        	}
        	else if (command.equalsIgnoreCase("ping")) {
        		if (Log.doTrace())
	        		Log.trace("TradeBrokerMDB:onMessage  received test command -- message: " + ((TextMessage)message).getText());
	        		
        		long publishTime = message.getLongProperty("publishTime");
				long receiveTime = System.currentTimeMillis();			

				TimerStat currentStats = mdbStats.addTiming("TradeBrokerMDB:ping", publishTime, receiveTime );
					
				if ( (currentStats.getCount() % statInterval) == 0)
				{
					Log.log(new java.util.Date()+ "\nTradeBrokerMDB: received 100 ping messages. " + 
  							"\nCurrent Ping Message Statistics\n\tTotal ping message count = " + currentStats.getCount() + 
							"\n\tTime to receive messages (in seconds):" +
							"\n\t\tmin: " +currentStats.getMinSecs()+
							"\n\t\tmax: " +currentStats.getMaxSecs()+
							"\n\t\tavg: " +currentStats.getAvgSecs()+
							"\n\n\n\tThe current message is:\n\t"+((TextMessage)message).getText());
	        	}
        	}
        	else
        		Log.error("TradeBrokerMDB:onMessage - unknown message request command-->" + command + "<-- message=" + ((TextMessage)message).getText());
        } 
        catch (Throwable t) 
        {
        	//JMS onMessage should handle all exceptions
			Log.error("TradeBrokerMDB: Error rolling back transaction", t);			
            mdc.setRollbackOnly();
        }
    }  
    
    private TradeServices getTrade(boolean direct)
    throws Exception
    {
    	TradeServices trade;
       	if (direct)
			trade = new TradeDirect();
		else
			trade = tradeHome.create();

		return trade;
    }
    

    public TradeBrokerMDB() {
        if (Log.doTrace()) Log.trace("TradeBrokerMDB:TradeBrokerMDB()");
    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
        if (Log.doTrace()) Log.trace("TradeBrokerMDB:setMessageDriventContext()");
		this.mdc = mdc;		
    }

    public void ejbCreate() {
        if (Log.doTrace()) Log.trace("TradeBrokerMDB:ejbCreate()");
		try {
			InitialContext ic = new InitialContext();
			tradeHome 	= (TradeHome) ic.lookup("java:comp/env/ejb/Trade");
			statInterval = ( (Integer) ic.lookup("java:comp/env/statInterval") ).intValue();
			if ( statInterval <= 0 ) statInterval = 100;
			mdbStats = MDBStats.getInstance();
		} catch (Exception e) {
			Log.error("TradeBrokerMDB:ejbCreate Lookup of Local Entity Homes Failed\n" + e);
		}        
    }
       
    public void ejbRemove() {
        if (Log.doTrace()) Log.trace("TradeBrokerMDB:ejbRemove()");
    }
}