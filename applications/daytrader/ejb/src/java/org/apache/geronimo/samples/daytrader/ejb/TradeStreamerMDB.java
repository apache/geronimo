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

public class TradeStreamerMDB 
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
        		Log.trace("TradeStream:onMessage -- received message -->" 
        			+ ((TextMessage)message).getText() + "command-->" 
        			+	message.getStringProperty("command") + "<--");
        	String command = message.getStringProperty("command");
        	if (command==null) 
        	{
        		Log.debug("TradeStreamerMDB:onMessage -- received message with null command. Message-->"+message);
        		return;
	        }
            if (command.equalsIgnoreCase("updateQuote")) 
            {
        	if (Log.doTrace()) 
        		Log.trace("TradeStreamer:onMessage -- received message -->" 
        			+ ((TextMessage)message).getText()
        			+ "\n\t symbol = " + message.getStringProperty("symbol")
        			+ "\n\t current price =" + message.getStringProperty("price")
        			+ "\n\t old price =" + message.getStringProperty("oldPrice")
        			);
        		long publishTime = message.getLongProperty("publishTime");
				long receiveTime = System.currentTimeMillis();			
				
				TimerStat currentStats = mdbStats.addTiming("TradeBrokerStreamer:udpateQuote", publishTime, receiveTime );
				
				if ( (currentStats.getCount() % statInterval) == 0)
				{
					Log.log(new java.util.Date()+ "\nTradeStreamerMDB: 100 Trade stock prices updated:  " + 
  							"\nCurrent Statistics\n\tTotal update Quote Price message count = " + currentStats.getCount() + 
							"\n\tTime to receive stock update alerts messages (in seconds):" +
							"\n\t\tmin: " +currentStats.getMinSecs()+
							"\n\t\tmax: " +currentStats.getMaxSecs()+
							"\n\t\tavg: " +currentStats.getAvgSecs()+
							"\n\n\n\tThe current price update is:\n\t"+((TextMessage)message).getText()) ;
	        	}
        	}
        	else if (command.equalsIgnoreCase("ping")) {
        		if (Log.doTrace())
	        		Log.trace("TradeStreamerMDB:onMessage  received ping command -- message: " + ((TextMessage)message).getText());	        		

        		long publishTime = message.getLongProperty("publishTime");
				long receiveTime = System.currentTimeMillis();			

				TimerStat currentStats = mdbStats.addTiming("TradeStreamerMDB:ping", publishTime, receiveTime );

				if ( (currentStats.getCount() % statInterval) == 0)
				{
					Log.log(new java.util.Date()+ "\nTradeStreamerMDB: received 100 ping messages. " + 
  							"\nCurrent Ping Message Statistics\n\tTotal ping message count = " + currentStats.getCount() + 
							"\n\tTime to receive messages (in seconds):" +
							"\n\t\tmin: " +currentStats.getMinSecs()+
							"\n\t\tmax: " +currentStats.getMaxSecs()+
							"\n\t\tavg: " +currentStats.getAvgSecs()+
							"\n\n\n\tThe current message is:\n\t"+((TextMessage)message).getText());
				}
        	}
        	else
        		Log.error("TradeStreamerMDB:onMessage - unknown message request command-->" + command + "<-- message=" + ((TextMessage)message).getText());
        } 
        catch (Throwable t) 
        {
        	//JMS onMessage should handle all exceptions
			Log.error("TradeStreamerMDB: Exception", t);			
			//UPDATE - Not rolling back for now -- so error messages are not redelivered
            //mdc.setRollbackOnly();
        }
    }      	
    	

    public TradeStreamerMDB() {
        if (Log.doTrace()) Log.trace("TradeStreamerMDB:TradeStreamerMDB()");
    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
        if (Log.doTrace()) Log.trace("TradeStreamerMDB:setMessageDriventContext()");
		this.mdc = mdc;		
    }

    public void ejbCreate() {
        if (Log.doTrace()) Log.trace("TradeStreamerMDB:ejbCreate()");
		try {
			InitialContext ic = new InitialContext();
			statInterval = ( (Integer) ic.lookup("java:comp/env/statInterval") ).intValue();
			if ( statInterval <= 0 ) statInterval = 100;
			mdbStats = MDBStats.getInstance();
		} catch (Exception e) {
			Log.error("TradeStreamerMDB:ejbCreate Lookup of EJB environment failed\n" + e);
		}        
    }
       
    public void ejbRemove() {
        if (Log.doTrace()) Log.trace("TradeStreamerMDB:ejbRemove()");
    }
}