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

import java.util.*;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.geronimo.samples.daytrader.ejb.*;

import org.apache.geronimo.samples.daytrader.*;

public class TradeClient implements Runnable {

	public static final int DEFAULT_UPDATE_INTERVAL = 2;
	public static final int DEFAULT_MAX_PER_SECOND = 10;

	// Various components
	private TradeQuoteAuditStats auditStats;
	private TradeClientMessageListener listener;
	private TradeClientGUI gui;
	private int maxPerSecond = DEFAULT_MAX_PER_SECOND;

	private static TradeClient tradeClient;

	// EJB values
	private InitialContext initial;
	private Trade trade;
	private boolean useENC = true;

	// Updater thread
	private int updateInterval = DEFAULT_UPDATE_INTERVAL;

	public static void main(String[] args) {
		try	{
			TradeClient streamer  = new TradeClient();
			if (args.length > 0) {
				if (args[0].equals("-noENC")) {
					streamer.useENC = false;
				}
				else {
					System.out.println("Usage TradeClient [-noENC]");
					System.exit(1);
				}
			}

			tradeClient = streamer;
			streamer.startClient();
		}
		catch (Exception e)	{
			System.err.println("Caught an unexpected exception!");
			e.printStackTrace();
		}
	}

	public static TradeClient getTradeClient() {
		return tradeClient;
	}

	public void startClient() throws Exception {
		auditStats = new TradeQuoteAuditStats();
		setupEJB();
		listener = new TradeClientMessageListener(this, useENC);
		listener.subscribe();
		resetStatsFromServer();
		gui = new TradeClientGUI(this);
		gui.show();
		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
	}

	public TradeQuoteAuditStats getAuditStats() {
		return auditStats;
	}

	public void reset() throws Exception {
		resetStatsFromServer();
	}

	public void resetStatsFromServer() throws Exception {
		auditStats.clearStats();
		Collection quotes = trade.getAllQuotes();

		for (Iterator it = quotes.iterator(); it.hasNext(); ) {
			QuoteDataBean bean = (QuoteDataBean)it.next();
			auditStats.updateSymbol(bean.getSymbol(), bean.getCompanyName(), bean.getPrice(), bean.getOpen(), bean.getLow(), bean.getHigh(), bean.getVolume(), System.currentTimeMillis(), bean.getPrice(), bean.getVolume());
		}
	}

	public void updateStatusMessage(String message) {
		gui.updateStatusMessage(message);
	}

	public InitialContext getInitialContext() {
		return initial;
	}

	public void setupEJB() throws Exception {
		initial = new InitialContext();
		Object objref;
		if (useENC) {
			objref = initial.lookup("java:comp/env/ejb/Trade");
		}
		else {
			objref = initial.lookup("ejb/TradeEJB");
		}
		TradeHome home = (TradeHome)PortableRemoteObject.narrow(objref, TradeHome.class);
		trade = home.create();
	}

	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	public void closeClient() {
		System.exit(1);
	}

	public void run() {
		while(true) {
			try {
				Thread.sleep((long)updateInterval*1000);
			}
			catch (InterruptedException ie) {
			}
			auditStats.fireTableDataChanged();
		}
	}
}
