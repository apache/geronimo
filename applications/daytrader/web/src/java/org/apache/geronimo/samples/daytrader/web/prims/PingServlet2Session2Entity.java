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

package org.apache.geronimo.samples.daytrader.web.prims;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import org.apache.geronimo.samples.daytrader.ejb.*;
import org.apache.geronimo.samples.daytrader.util.*;

import org.apache.geronimo.samples.daytrader.*;

/**
 *
 * PingServlet2Session2Entity tests key functionality of a servlet call to a 
 * stateless SessionEJB, and then to a Entity EJB representing data in a database.
 * This servlet makes use of the Stateless Session EJB {@link Trade}, and then
 * uses {@link TradeConfig} to generate a random stock symbol.  The stocks
 * price is looked up using the Quote Entity EJB.  
 *
 */
public class PingServlet2Session2Entity extends HttpServlet
{

	private static String initTime;
	private static int hitCount;
	private static TradeHome tradeHome;

	/**
	 * forwards post requests to the doGet method
	 * Creation date: (11/6/2000 10:52:39 AM)
	 * @param res javax.servlet.http.HttpServletRequest
	 * @param res2 javax.servlet.http.HttpServletResponse
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		doGet(req, res);
	}

	/**
	* this is the main method of the servlet that will service all get requests.
	* @param request HttpServletRequest
	* @param responce HttpServletResponce
	**/
	public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws IOException, ServletException
	{

		res.setContentType("text/html");
		java.io.PrintWriter out = res.getWriter();
		String symbol = null;
		QuoteDataBean quoteData = null;
		Trade trade = null;
		StringBuffer output = new StringBuffer(100);

		output.append(
			"<html><head><title>PingServlet2Session2Entity</title></head>"
				+ "<body><HR><FONT size=\"+2\" color=\"#000066\">PingServlet2Session2Entity<BR></FONT>"
				+ "<FONT size=\"-1\" color=\"#000066\">"
				+ "PingServlet2Session2Entity tests the common path of a Servlet calling a Session EJB "
				+ "which in turn calls an Entity EJB.<BR>");

		try
			{

			//only want to do this once.
			if (tradeHome == null)
				{
				//only want one thread to create the EjbHome
				synchronized (lock)
				{

					if (tradeHome == null)
						{

						//out.println("doing JNDI lookup and creating new reference to trade.TradeHome");
						output.append("<HR><B>Performing JNDI lookup to create new tradeHome</B>");

						try
							{
							//I am going to use the System env. that is set through TradeConfig
							InitialContext ic = new InitialContext();

							tradeHome =
								(TradeHome) PortableRemoteObject.narrow(
									ic.lookup("java:comp/env/ejb/Trade"),
									TradeHome.class);
						}
						catch (Exception ne)
							
						{
							Log.error(ne, "PingServlet2Session2Entity.goGet(...): exception caught looking up and narrowing 'TradeHome'");
							throw ne;
						}
					}
				}
			}

			try
				{
				int iter = TradeConfig.getPrimIterations();
				for (int ii = 0; ii < iter; ii++) {
					//I have the TradeBean Home, now I want to get an instance every time
					symbol = TradeConfig.rndSymbol();
					trade = tradeHome.create();
					//getQuote will call findQuote which will instaniate the Quote Entity Bean
					//and then will return a QuoteObject
					quoteData = trade.getQuote(symbol);
					trade.remove();
				}
			}
			catch (Exception ne)
				{
				Log.error(ne, "PingServlet2Session2Entity.goGet(...): exception getting QuoteData through Trade");
				throw ne;
			}

			output.append("<HR>initTime: " + initTime).append(
				"<BR>Hit Count: " + hitCount++);
			output.append("<HR>Quote Information<BR><BR>" + quoteData.toHTML());
			out.println(output.toString());

		}
		catch (Exception e)
			{
			Log.error(e, "PingServlet2Session2Entity.doGet(...): General Exception caught");
			res.sendError(500, "General Exception caught, " + e.toString());
		}
	}

	/** 
	 * returns a string of information about the servlet
	 * @return info String: contains info about the servlet
	 **/

	public String getServletInfo()
	{
		return "web primitive, tests Servlet to Session to Entity EJB path";

	}
	/**
	* called when the class is loaded to initialize the servlet
	* @param config ServletConfig:
	**/
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		hitCount = 0;
		tradeHome = null;
		initTime = new java.util.Date().toString();
		//this is for synchronization
		lock = new Integer(99);
	}
	private java.lang.Integer lock;
}