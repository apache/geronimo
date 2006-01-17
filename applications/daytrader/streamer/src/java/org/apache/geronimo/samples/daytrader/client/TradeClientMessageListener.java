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

package org.apache.geronimo.samples.daytrader.client;


import javax.naming.InitialContext;
import javax.jms.*;

import org.apache.geronimo.samples.daytrader.util.*;

import java.math.*;

public class TradeClientMessageListener implements MessageListener {
	private TradeClient client;
	private boolean useENC;

	public TradeClientMessageListener(TradeClient client, boolean useENC) {
		this.client = client;
		this.useENC = useENC;
	}

	public void subscribe() {
		try	{
			System.out.println("TradeStreamer getInitial Context");
			InitialContext context = client.getInitialContext();
			
			Log.trace("TradeStreamer pub/sub JNDI starting");
			ConnectionFactory connFactory;
			if (useENC) {
				connFactory = (ConnectionFactory) context.lookup("java:comp/env/jms/TopicConnectionFactory");
			}
			else {
				connFactory = (ConnectionFactory) context.lookup("jms/TopicConnectionFactory");
			}

			Topic streamerTopic;
			if (useENC) {
				streamerTopic = (Topic) context.lookup("java:comp/env/jms/TradeStreamerTopic");
			}
			else {
				streamerTopic = (Topic) context.lookup("jms/TradeStreamerTopic");
			}

			Log.trace("TradeStreamer pub/sub JNDI ending");

			Connection conn = connFactory.createConnection();
			Log.trace("TradeStreamer pub/sub after create Topic");
			Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Log.trace("TradeStreamer pub/sub after create session");
			MessageConsumer consumer = sess.createConsumer(streamerTopic);
			Log.trace("TradeStreamer pub/sub after create sub");
			conn.start();
			Log.trace("TradeStreamer pub/sub after tconn startc");
			consumer.setMessageListener(this);
			Log.trace("TradeStreamer pub/sub after set message listener");
			
			consumer = sess.createConsumer(streamerTopic);
			Log.trace("TradeStreamer pub/sub listener registered successfully");
		}
		catch (Exception e)	{
			Log.error("TradeStreamer Subscribe Exception: " + e);
		}
	}

	public void onMessage(Message message) {
		TextMessage msg = null;
		try	{
			if (message instanceof TextMessage) {
				msg = (TextMessage) message;

				long recvTime = System.currentTimeMillis();
				String command = message.getStringProperty("command");
				
				if ( (command !=null) && (command.equalsIgnoreCase("updateQuote")) ) {
					String symbol = message.getStringProperty("symbol");
					String company = message.getStringProperty("company");
					BigDecimal price = new BigDecimal(message.getStringProperty("price"));				
					BigDecimal oldPrice = new BigDecimal(message.getStringProperty("oldPrice"));							
					BigDecimal open = new BigDecimal(message.getStringProperty("open"));
					BigDecimal low = new BigDecimal(message.getStringProperty("low"));
					BigDecimal high = new BigDecimal(message.getStringProperty("high"));
					double volume = message.getDoubleProperty("volume");		
				
					BigDecimal changeFactor = new BigDecimal(message.getStringProperty("changeFactor"));		
					double sharesTraded = message.getDoubleProperty("sharesTraded");				
					long publishTime = message.getLongProperty("publishTime");

					TradeQuoteAuditStats stats = client.getAuditStats();
					BigDecimal priceDiff = price.subtract(oldPrice);
					stats.updateSymbol(symbol, company, price, open, low, high, volume, publishTime, priceDiff, sharesTraded);
					/*			
								jTextArea1.append("Streamer--> " + new java.util.Date() + " Stock: " + symbol  + " ");
								BigDecimal diff = price.subtract(oldPrice);
								String direction = null;
								if (diff.doubleValue() < 0.0)
								direction = "DOWN";
								else
								direction = "UP";
		        	
								jTextArea1.append(price + " (" + diff + ") " + direction  + "\n");
					*/
				}
				else {
					System.out.println("msg = " + msg + " action = " + msg.getStringProperty("action"));
					client.updateStatusMessage(msg.getText());
				}
			}
			else {
				System.out.println("Message of wrong type: " + message.getClass().getName());
			}
		}
		catch (JMSException e) {
			System.out.println("JMSException in onMessage(): " + e.toString());
		}
		catch (Throwable t) {
			System.out.println("Exception in onMessage():" + t.getMessage());
		}
	}
}
