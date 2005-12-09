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
import java.util.Collection;
import java.util.Iterator;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;

import org.apache.geronimo.samples.daytrader.ejb.*;
import org.apache.geronimo.samples.daytrader.util.*;

import org.apache.geronimo.samples.daytrader.*;

/**
 * Primitive to test Entity Container Managed Relationshiop  One to One
 * Servlet will generate a random userID and get the profile for that user using a {@link trade.Account} Entity EJB 
 * This tests the common path of a Servlet calling a Session to Entity EJB to get CMR One to One data
 *
 */

public class PingServlet2Session2CMROne2Many extends HttpServlet
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

		Trade  trade = null;
		String userID = null;

		StringBuffer output = new StringBuffer(100);
		output.append(
			"<html><head><title>Servlet2Session2CMROne20ne</title></head>"
				+ "<body><HR><FONT size=\"+2\" color=\"#000066\">PingServlet2Session2CMROne2Many<BR></FONT>"
				+ "<FONT size=\"-1\" color=\"#000066\"><BR>PingServlet2Session2CMROne2Many uses the Trade Session EJB"
				+ " to get the orders for a user using an EJB 2.0 Entity CMR one to many relationship");
		try
			{
			//this is just a large general catch block.

			//it is important only to look up the home once.
			if (tradeHome == null)
				{
				//make sure that only one thread looks up the home
				synchronized (lock)
				{
					if (tradeHome == null)
						{
						output.append(
							"<HR>Performing JNDI lookup and creating reference to TradeHome</B>");
						try
							{
							//do not pass an environment so that it uses the system env.
							InitialContext ic = new InitialContext();
							//lookup and narrow (cast) the reference to the ejbHome.  
							tradeHome = (TradeHome) ic.lookup("java:comp/env/ejb/Trade");
						}
						catch (Exception ne)
							{
							//wrap and throw the exception for handling
							Log.error(ne,"PingServlet2EntityCMROne2Many.doGet(...): error looking up TradeHome");
							throw ne;
						}
					}
				}
			}
			
			Collection orderDataBeans = null;
			int iter = TradeConfig.getPrimIterations();
			for (int ii = 0; ii < iter; ii++) {
				userID = TradeConfig.rndUserID();
				trade = tradeHome.create();			

				//get the users orders and print the output.
				orderDataBeans = trade.getOrders(userID);
			}			

			output.append("<HR>initTime: " + initTime + "<BR>Hit Count: ").append(
				hitCount++);
			output
				.append("<HR>One to Many CMR access of Account Orders from Account Entity<BR> ");
			output.append("<HR>User: " + userID + " currently has " + orderDataBeans.size() + " stock orders:");
			Iterator it = orderDataBeans.iterator();
			while ( it.hasNext() )
			{
				OrderDataBean orderData = (OrderDataBean) it.next();
				output.append("<BR>" + orderData.toHTML());
			}				
			output.append("</font><HR></body></html>");
			out.println(output.toString());
		}
		catch (Exception e)
			{
			Log.error(e,"PingServlet2Session2CMROne2Many.doGet(...): error");
			//this will send an Error to teh web applications defined error page.
			res.sendError(
				500,
				"PingServlet2Session2CMROne2Many.doGet(...): error"
					+ e.toString());

		}
	}

		/** 
		 * returns a string of information about the servlet
		 * @return info String: contains info about the servlet
		 **/

	public String getServletInfo()
	{
		return "web primitive, tests Servlet to Entity EJB path";
	}

	/**
		* called when the class is loaded to initialize the servlet
		* @param config ServletConfig:
		**/
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		hitCount = 0;
		initTime = new java.util.Date().toString();
		//set this to null, this will be initialized in the doGet method.
		tradeHome = null;
		//this lock is used to synchronize initialization of the EJBHome
		lock = new Integer(99);

	}
	private java.lang.Integer lock;
}