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
 * This primitive is designed to run inside the TradeApplication and relies upon
 * the {@link trade_client.TradeConfig} class to set configuration parameters.
 * PingServlet2SessionEJB tests key functionality of a servlet call to a 
 * stateless SessionEJB.
 * This servlet makes use of the Stateless Session EJB {@link trade.Trade} by calling
 * calculateInvestmentReturn with three random numbers.
 *
 */
public class PingServlet2Session extends HttpServlet {

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
	throws ServletException, IOException {
	doGet(req, res);
}
 
 
/**
* this is the main method of the servlet that will service all get requests.
* @param request HttpServletRequest
* @param responce HttpServletResponce
**/

public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws IOException, ServletException {

	res.setContentType("text/html");
	java.io.PrintWriter out = res.getWriter();
	Trade trade = null;
	//use a stringbuffer to avoid concatenation of Strings
	StringBuffer output = new StringBuffer(100);
	output.append(
		"<html><head><title>PingServlet2Session</title></head>"
			+ "<body><HR><FONT size=\"+2\" color=\"#000066\">PingServlet2Session<BR></FONT>"
			+ "<FONT size=\"-1\" color=\"#000066\">"
			+ "Tests the basis path from a Servlet to a Session Bean."); 

	//get a reference to the TradeBean (session bean) Home
	try
	{
		//we only want to look up the home once
		if (tradeHome == null)
		{
			//we only want one thread to create the EJBHome
			synchronized (lock)
		{
				if (tradeHome == null)
				{

					output.append("<HR><B>Performing JNDI lookup to create a TradeHome</B>");
					try
					{
						//I am going to use the sytem environment that is set by
						//trade_client.TradeConfig. 
						InitialContext ic = new InitialContext();

						tradeHome = 
							(TradeHome) PortableRemoteObject.narrow(
								ic.lookup("java:comp/env/ejb/Trade"), 
								TradeHome.class); 
					}
					catch (Exception ne)
					{
						Log.error(ne,"PingServlet2Session.doGet(...): errorj looking up TradeHome");
						throw ne;
					} //end of catch
				}
			}
		}
		//tradeHome will be a reference to TradeHome. 
		try
		{
			//create a new Trade instance
			trade = tradeHome.create();

			//create three random numbers 
			double rnd1 = Math.random() * 1000000;
			double rnd2 = Math.random() * 1000000;
			double rnd3 = Math.random() * 1000000;

			//use a function to do some work.
			double increase = 0.0;
			int iter = TradeConfig.getPrimIterations();
			for (int ii = 0; ii < iter; ii++) {
				increase =  trade.investmentReturn(rnd1, rnd2);
			}

			//write out the output
			output.append("<HR>initTime: " + initTime);
			output.append("<BR>Hit Count: " + hitCount++);
			output.append("<HR>Investment Return Information <BR><BR>investment: " + rnd1);
			output.append("<BR>current Value: " + rnd2);
			output.append(
				"<BR>investment return " + increase + "<HR></FONT></BODY></HTML>"); 
			out.println(output.toString());

		}
		catch (Exception e)
		{
			Log.error("PingServlet2Session.doGet(...):exception calling trade.investmentReturn "); 
			throw e;
		}
	} //this is where I actually handle the exceptions
	catch (Exception e)
	{
		Log.error(e, "PingServlet2Session.doGet(...): error"); 
		res.sendError(500, "PingServlet2Session.doGet(...): error, " + e.toString());

	}
}                           




/** 
 * returns a string of information about the servlet
 * @return info String: contains info about the servlet
 **/

public String getServletInfo()
{
	return "web primitive, configured with trade runtime configs, tests Servlet to Session EJB path"; 

}      
/**
* called when the class is loaded to initialize the servlet
* @param config ServletConfig:
**/
public void init(ServletConfig config) throws ServletException {
	super.init(config);
	hitCount = 0;
	initTime = new java.util.Date().toString();
	tradeHome = null;
	//for synchronization
	lock = new Integer(99);
}      




;


	private java.lang.Integer lock;}